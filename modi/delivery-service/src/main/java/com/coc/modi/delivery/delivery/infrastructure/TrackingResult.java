package com.coc.modi.delivery.delivery.infrastructure;

public record TrackingResult(
		String rawStatus,
		String description,
		boolean delivered
) {
}
