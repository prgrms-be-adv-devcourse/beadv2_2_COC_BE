package com.coc.modi.kafka.event;

import java.time.Instant;
import java.util.UUID;

public record ProductIndexEvent(
		String eventId,
		Instant occurredAt,
		Long productId,
		Action action
) {
	
	public enum Action {
		INDEX,
		DELETE
	}
	
	public static ProductIndexEvent index(Long productId) {
		return of(productId, Action.INDEX);
	}
	
	public static ProductIndexEvent delete(Long productId) {
		return of(productId, Action.DELETE);
	}
	
	private static ProductIndexEvent of(Long productId, Action action) {
		return new ProductIndexEvent(
				UUID.randomUUID().toString(),
				Instant.now(),
				productId,
				action
		);
	}
}
