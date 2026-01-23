package com.coc.modi.kafka.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record RentalClosedEvent(
		UUID eventId,
		Instant occurredAt,
		Long rentalItemId,
		Long memberId,
		Long sellerId,
		Long productId,
		BigDecimal rentalAmount,
		String type,
		LocalDateTime returnedAt,
		LocalDateTime refundedAt,
		int sequence
) {

	public static RentalClosedEvent of(Long rentalItemId,
									   Long memberId,
									   Long sellerId,
									   Long productId,
									   BigDecimal rentalAmount,
									   String type,
									   LocalDateTime returnedAt,
									   LocalDateTime refundedAt,
									   int sequence) {
		return new RentalClosedEvent(
				UUID.randomUUID(),
				Instant.now(),
				rentalItemId,
				memberId,
				sellerId,
				productId,
				rentalAmount,
				type,
				returnedAt,
				refundedAt,
				sequence
		);
	}
}
