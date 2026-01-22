package com.coc.modi.kafka.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProductModerationResultEvent(
		String eventId,
		Instant occurredAt,
		Long productId,
		String result,
		Double score,
		List<String> reasons,
		String message
) {
	public static ProductModerationResultEvent of(Long productId,
												  String result,
												  Double score,
												  List<String> reasons,
												  String message) {

		return new ProductModerationResultEvent(
				UUID.randomUUID().toString(),
				Instant.now(),
				productId,
				result,
				score,
				reasons,
				message
		);
	}
}
