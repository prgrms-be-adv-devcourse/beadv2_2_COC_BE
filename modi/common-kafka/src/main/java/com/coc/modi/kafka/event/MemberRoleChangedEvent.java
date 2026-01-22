package com.coc.modi.kafka.event;

import java.time.Instant;
import java.util.UUID;

public record MemberRoleChangedEvent(
		String eventId,
		Instant occurredAt,
		Long memberId,
		String role
) {

	public static MemberRoleChangedEvent of(Long memberId, String role) {

		return new MemberRoleChangedEvent(
				UUID.randomUUID().toString(),
				Instant.now(),
				memberId,
				role
		);
	}
}
