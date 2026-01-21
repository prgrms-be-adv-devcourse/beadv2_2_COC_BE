package com.coc.modi.kafka.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProductModerationRequestedEvent(
		String eventId,
		Instant occurredAt,
		Long productId,
		Long sellerId,
		String reason,
		String name,
		String description,
		List<String> specValues,
		List<String> imageUrls
) {
	public static ProductModerationRequestedEvent of(Long productId,
													 Long sellerId,
													 String reason,
													 String name,
													 String description,
													 List<String> specValues,
													 List<String> imageUrls) {
		
		return new ProductModerationRequestedEvent(
				UUID.randomUUID().toString(),
				Instant.now(),
				productId,
				sellerId,
				reason,
				name,
				description,
				specValues,
				imageUrls
		);
	}
}
