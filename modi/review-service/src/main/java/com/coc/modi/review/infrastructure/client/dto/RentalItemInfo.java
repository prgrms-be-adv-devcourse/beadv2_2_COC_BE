package com.coc.modi.review.infrastructure.client.dto;

import java.time.LocalDateTime;

public record RentalItemInfo(
		Long rentalItemId,
		Long productId,
		Long memberId,
		Long sellerId,
		String status,
		LocalDateTime returnedAt
) {
}
