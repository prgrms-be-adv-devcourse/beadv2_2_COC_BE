package com.coc.modi.kafka.event;

import java.time.Instant;
import java.util.UUID;

public record RentalReturnedEvent(
		String eventId,
		Instant occurredAt,
		Long rentalItemId,
		Long memberId,
		Long sellerId,
		Long productId,
		String status
) {

	public static RentalReturnedEvent of(
			Long rentalItemId,
			Long memberId,
			Long sellerId,
			Long productId,
			String status
	) {
		return new RentalReturnedEvent(
				UUID.randomUUID().toString(),
				Instant.now(),
				rentalItemId,
				memberId,
				sellerId,
				productId,
				status
		);
	}
}
