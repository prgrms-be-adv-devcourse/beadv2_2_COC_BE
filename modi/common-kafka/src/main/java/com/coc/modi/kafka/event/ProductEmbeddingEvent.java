package com.coc.modi.kafka.event;

import java.time.Instant;
import java.util.UUID;

public record ProductEmbeddingEvent(
		String eventId,
		Instant occurredAt,
		Long productId,
		Action action
) {
	
	public enum Action {
		UPDATE
	}
	
	public static ProductEmbeddingEvent update(Long productId) {
		
		return new ProductEmbeddingEvent(
				UUID.randomUUID().toString(),
				Instant.now(),
				productId,
				Action.UPDATE
		);
	}
}
