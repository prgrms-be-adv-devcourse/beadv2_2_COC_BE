package com.coc.modi.review.infrastructure.client.dto;

public record RentalItemInfo(
		Long rentalItemId,
		Long productId,
		Long memberId,
		Long sellerId,
		String status
) {
}
