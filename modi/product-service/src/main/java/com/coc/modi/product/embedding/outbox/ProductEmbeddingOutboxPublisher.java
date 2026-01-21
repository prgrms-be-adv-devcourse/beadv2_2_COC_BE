package com.coc.modi.product.embedding.outbox;

import java.util.List;

import com.coc.modi.kafka.event.ProductEmbeddingEvent;
import com.coc.modi.product.product.exception.ProductEmbeddingOutboxException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEmbeddingOutboxPublisher {
	
	private final ProductEmbeddingOutboxEventRepository outboxEventRepository;
	private final KafkaTemplate<String, Object> kafkaTemplate;
	private final ObjectMapper objectMapper;
	
	@Value("${outbox.publisher.batch-size:50}")
	private int batchSize;
	
	@Value("${outbox.publisher.max-retries:10}")
	private int maxRetries;
	
	@Scheduled(fixedDelayString = "${outbox.publisher.delay-ms:1000}")
	@Transactional
	public void publishPendingEvents() {
		List<ProductEmbeddingOutboxEvent> events = outboxEventRepository.findPendingForPublish(batchSize);
		for (ProductEmbeddingOutboxEvent event : events) {
			try {
				publishEvent(event);
				event.markSent();
			} catch (Exception ex) {
				event.markFailed(ex.getMessage(), maxRetries);
				log.warn("Outbox publish failed. id={}, type={}, retryCount={}",
						event.getId(), event.getEventType(), event.getRetryCount(), ex);
			}
		}
	}
	
	private void publishEvent(ProductEmbeddingOutboxEvent event) throws Exception {
		if (event.getEventType() == ProductEmbeddingOutboxEventType.PRODUCT_EMBEDDING_EVENT) {
			ProductEmbeddingEvent payload = readPayload(event.getPayload(), ProductEmbeddingEvent.class);
			kafkaTemplate
					.send(event.getEventType().getTopic(), payload.productId().toString(), payload)
					.get();
			return;
		}
		throw new ProductEmbeddingOutboxException("지원하지 않는 아웃박스 이벤트 타입입니다: " + event.getEventType(), null);
	}
	
	private <T> T readPayload(String payload, Class<T> target) {
		try {
			return objectMapper.readValue(payload, target);
		} catch (JsonProcessingException ex) {
			throw new ProductEmbeddingOutboxException("아웃박스 payload 역직렬화에 실패했습니다.", ex);
		}
	}
}
