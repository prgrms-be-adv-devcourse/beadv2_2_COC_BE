package com.coc.modi.kafka.event;

import java.time.Instant;
import java.util.UUID;

public record SellerApprovedEvent(
		String eventId,
		Instant occurredAt,
		Long sellerId,
		Long memberId
) {

	public static SellerApprovedEvent of(Long sellerId, Long memberId) {

		return new SellerApprovedEvent(
				UUID.randomUUID().toString(),
				Instant.now(),
				sellerId,
				memberId
		);
	}
}
