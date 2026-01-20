package com.coc.modi.ai.embedding.application;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.coc.modi.ai.embedding.infrastructure.ProductEmbeddingRepository;
import com.coc.modi.ai.embedding.infrastructure.client.ProductEmbeddingClient;
import com.coc.modi.ai.event.KafkaProductEmbeddingEventPublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductEmbeddingReindexService {
	
	private final ProductEmbeddingClient productEmbeddingClient;
	private final ProductEmbeddingRepository productEmbeddingRepository;
	private final KafkaProductEmbeddingEventPublisher embeddingEventPublisher;
	
	public int reindexMissing() {
		
		List<Long> productIds = productEmbeddingClient.getEmbeddingTargetIds();
		if (productIds == null || productIds.isEmpty()) {
			return 0;
		}
		
		Set<Long> embeddedIds = new HashSet<>(productEmbeddingRepository.findProductIdsWithEmbedding());
		
		int count = 0;
		for (Long productId : productIds) {
			if (productId == null || embeddedIds.contains(productId)) {
				continue;
			}
			embeddingEventPublisher.publishUpdate(productId);
			count++;
		}
		log.info("임베딩 재색인 요청 완료. total={} missing={}", productIds.size(), count);
		return count;
	}
	
	public boolean reindexMissingOne(Long productId) {
		
		if (productId == null) {
			return false;
		}
		
		if (productEmbeddingRepository.hasEmbedding(productId)) {
			return false;
		}
		
		try {
			if (productEmbeddingClient.getEmbeddingTarget(productId) == null) {
				return false;
			}
		} catch (Exception ex) {
			log.warn("임베딩 재색인 대상 조회 실패. productId={}", productId, ex);
			return false;
		}
		
		embeddingEventPublisher.publishUpdate(productId);
		return true;
	}
}
