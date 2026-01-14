package com.coc.modi.kafka.event;

import java.time.Instant;
import java.util.UUID;

public record SellerRejectedEvent(
		String eventId,
		Instant occurredAt,
		Long sellerId,
		Long memberId,
		String email
) {

	public static SellerRejectedEvent of(Long sellerId, Long memberId, String email) {

		return new SellerRejectedEvent(
				UUID.randomUUID().toString(),
				Instant.now(),
				sellerId,
				memberId,
				email
		);
	}
}
