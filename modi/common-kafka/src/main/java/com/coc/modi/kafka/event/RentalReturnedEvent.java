package com.coc.modi.kafka.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record RentalReturnedEvent(
		String eventId,
		Instant occurredAt,
		Long rentalItemId,
		Long memberId,
		Long sellerId,
		Long productId,
		BigDecimal rentalAmount,
		String status,
		LocalDateTime returnedAt
) {

	public static RentalReturnedEvent of(
			Long rentalItemId,
			Long memberId,
			Long sellerId,
			Long productId,
			BigDecimal rentalAmount,
			String status,
			LocalDateTime returnedAt
	) {
		return new RentalReturnedEvent(
				UUID.randomUUID().toString(),
				Instant.now(),
				rentalItemId,
				memberId,
				sellerId,
				productId,
				rentalAmount,
				status,
				returnedAt
		);
	}
}
