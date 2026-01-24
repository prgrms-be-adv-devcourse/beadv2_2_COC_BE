package com.coc.modi.kafka.event;

import java.time.Instant;
import java.util.UUID;

public record SellerRegistrationApprovedEvent(
		String eventId,
		Instant occurredAt,
		Long registrationId,
		Long memberId,
		String email
) {

	public static SellerRegistrationApprovedEvent of(Long registrationId, Long memberId, String email) {

		return new SellerRegistrationApprovedEvent(
				UUID.randomUUID().toString(),
				Instant.now(),
				registrationId,
				memberId,
				email
		);
	}
}
