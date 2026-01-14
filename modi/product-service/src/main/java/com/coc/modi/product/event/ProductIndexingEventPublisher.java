package com.coc.modi.product.event;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductIndexingEventPublisher {

	private final KafkaProductIndexEventPublisher productIndexEventPublisher;
	private final KafkaProductEmbeddingEventPublisher productEmbeddingEventPublisher;

	public void publishIndex(Long productId) {
		if (productId == null) {
			return;
		}
		productIndexEventPublisher.publishIndex(productId);
	}

	public void publishIndexAndEmbedding(Long productId) {
		if (productId == null) {
			return;
		}
		productIndexEventPublisher.publishIndex(productId);
		productEmbeddingEventPublisher.publishUpdate(productId);
	}

	public void publishDelete(Long productId) {
		if (productId == null) {
			return;
		}
		productIndexEventPublisher.publishDelete(productId);
	}

	public void publishEmbeddingUpdate(Long productId) {
		if (productId == null) {
			return;
		}
		productEmbeddingEventPublisher.publishUpdate(productId);
	}
}
