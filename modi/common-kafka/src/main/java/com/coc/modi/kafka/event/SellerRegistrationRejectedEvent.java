package com.coc.modi.kafka.event;

import java.time.Instant;
import java.util.UUID;

public record SellerRegistrationRejectedEvent(
		String eventId,
		Instant occurredAt,
		Long registrationId,
		Long memberId,
		String email
) {

	public static SellerRegistrationRejectedEvent of(Long registrationId, Long memberId, String email) {

		return new SellerRegistrationRejectedEvent(
				UUID.randomUUID().toString(),
				Instant.now(),
				registrationId,
				memberId,
				email
		);
	}
}
