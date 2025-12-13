package com.coc.modi.kafka.event;

import java.time.Instant;
import java.util.UUID;

public record NotificationEvent(
		String eventId,
		Instant occurredAt,
		Long receiverId,
		String type,
		String title,
		String content,
		String referenceType,
		String referenceId
) {
	
	public static NotificationEvent of(
			Long receiverId,
			String type,
			String title,
			String content,
			String referenceType,
			String referenceId
	) {
		
		return new NotificationEvent(
				UUID.randomUUID().toString(),
				Instant.now(),
				receiverId,
				type,
				title,
				content,
				referenceType,
				referenceId
		);
	}
}
