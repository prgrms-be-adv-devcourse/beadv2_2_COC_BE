package com.coc.modi.review.application.dto;

public record CreateReviewCommand(
		Long rentalId,
		Long sellerId,
		Long memberId,
		Short rating,
		String content
) {
}
