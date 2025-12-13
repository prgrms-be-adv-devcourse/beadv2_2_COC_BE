package com.coc.modi.product.search.infrastructure;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.coc.modi.product.product.exception.ProductSearchUnavailableException;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ElasticsearchClientManager {
	
	private final String elasticUrl;
	private final ElasticsearchStatus status;
	
	private final AtomicReference<ElasticsearchClient> clientRef = new AtomicReference<>();
	private final AtomicReference<ElasticsearchTransport> transportRef = new AtomicReference<>();
	private final AtomicReference<RestClient> lowLevelClientRef = new AtomicReference<>();
	
	public ElasticsearchClientManager(
			@Value("${spring.elasticsearch.uris}") String elasticUrl,
			ElasticsearchStatus status
	) {
		
		this.elasticUrl = elasticUrl;
		this.status = status;
	}
	
	public ElasticsearchClient getClient() {
		
		ElasticsearchClient existing = clientRef.get();
		
		if (existing != null && status.isAvailable()) {
			
			return existing;
		}
		
		synchronized (this) {
			existing = clientRef.get();
			if (existing != null && status.isAvailable()) {
				return existing;
			}
			
			log.info("Elasticsearch 클라이언트 새로 생성 & 연결 시도. url={}", elasticUrl);
			closeCurrent();
			
			try {
				RestClient lowLevel = RestClient.builder(HttpHost.create(this.elasticUrl)).build();
				ElasticsearchTransport transport = new RestClientTransport(lowLevel, new JacksonJsonpMapper());
				ElasticsearchClient client = new ElasticsearchClient(transport);
				
				client.ping();
				
				lowLevelClientRef.set(lowLevel);
				transportRef.set(transport);
				clientRef.set(client);
				status.markAvailable();
				
				log.info("Elasticsearch 연결 성공");
				
				return client;
			} catch (Exception e) {
				status.markUnavailable(e);
				log.warn("Elasticsearch 연결 실패. url={}", elasticUrl, e);
				throw new ProductSearchUnavailableException("상품 검색 기능을 일시적으로 사용할 수 없습니다.", e);
			}
		}
	}
	
	private void closeCurrent() {
		
		ElasticsearchTransport transport = transportRef.getAndSet(null);
		RestClient lowLevelClient = lowLevelClientRef.getAndSet(null);
		clientRef.set(null);
		
		try {
			if (transport != null) {
				transport.close();
			}
		} catch (IOException e) {
			log.debug("ElasticsearchTransport close 실패", e);
		}
		
		try {
			if (lowLevelClient != null) {
				lowLevelClient.close();
			}
		} catch (IOException e) {
			log.debug("RestClient close 실패", e);
		}
	}
}
