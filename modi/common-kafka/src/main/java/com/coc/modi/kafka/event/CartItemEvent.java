package com.coc.modi.kafka.event;

import java.time.Instant;
import java.util.UUID;

public record CartItemEvent(
		String eventId,
		Instant occurredAt,
		Long memberId,
		Long productId,
		Long cartItemId,
		CartItemEventAction action
) {
	
	public static CartItemEvent added(Long memberId, Long productId, Long cartItemId) {
		return new CartItemEvent(
				UUID.randomUUID().toString(),
				Instant.now(),
				memberId,
				productId,
				cartItemId,
				CartItemEventAction.ADDED
		);
	}

	public static CartItemEvent removed(Long memberId, Long productId, Long cartItemId) {
		return new CartItemEvent(
				UUID.randomUUID().toString(),
				Instant.now(),
				memberId,
				productId,
				cartItemId,
				CartItemEventAction.REMOVED
		);
	}
}
