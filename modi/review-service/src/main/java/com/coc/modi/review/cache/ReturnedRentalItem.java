package com.coc.modi.review.cache;

public record ReturnedRentalItem(
		Long rentalItemId,
		Long memberId,
		Long sellerId,
		Long productId,
		String status
) {
}
