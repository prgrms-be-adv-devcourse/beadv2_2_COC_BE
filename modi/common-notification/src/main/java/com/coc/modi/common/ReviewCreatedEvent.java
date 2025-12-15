package com.coc.modi.common;

import java.time.LocalDateTime;

public record ReviewCreatedEvent(
		Long reviewId,
		Long rentalItemId,
		Long sellerId,
		Long memberId,
		int rating,
		String content,
		LocalDateTime createdAt
) {
}
