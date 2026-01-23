package com.coc.modi.product.outbox;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEmbeddingReindexScheduler {
	
	private final ProductOutboxEventRepository outboxEventRepository;
	private final ProductOutboxService outboxService;
	
	@Value("${embedding.reindex.batch-size:1000}")
	private int batchSize;
	
	@Scheduled(cron = "${embedding.reindex.cron:0 0 3 * * *}", zone = "${embedding.reindex.zone:Asia/Seoul}")
	public void reindexUpdatedProducts() {
		List<Long> productIds = outboxEventRepository.findEmbeddingReindexTargets(batchSize);
		if (productIds.isEmpty()) {
			return;
		}
		for (Long productId : productIds) {
			outboxService.enqueueEmbeddingUpdate(productId);
		}
		log.info("product_embedding_reindex_enqueued",
				kv("log_type", "service"),
				kv("product.count", productIds.size()));
	}
}
