package com.coc.modi.product.embedding.outbox;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEmbeddingReindexScheduler {
	
	private final ProductEmbeddingOutboxEventRepository outboxEventRepository;
	private final ProductEmbeddingOutboxService outboxService;
	
	@Value("${embedding.reindex.batch-size:1000}")
	private int batchSize;
	
	@Scheduled(cron = "${embedding.reindex.cron:0 0 3 * * *}", zone = "${embedding.reindex.zone:Asia/Seoul}")
	public void reindexUpdatedProducts() {
		List<Long> productIds = outboxEventRepository.findReindexTargets(batchSize);
		if (productIds.isEmpty()) {
			return;
		}
		for (Long productId : productIds) {
			outboxService.enqueueUpdate(productId);
		}
		log.info("상품 임베딩 재발행 대상 처리 완료. count={}", productIds.size());
	}
}
