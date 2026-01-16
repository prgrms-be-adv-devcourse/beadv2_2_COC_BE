package com.coc.modi.review.cache;

import java.time.LocalDateTime;

public record ReturnedRentalItem(
		Long rentalItemId,
		Long memberId,
		Long sellerId,
		Long productId,
		String status,
		LocalDateTime returnedAt
) {
}
