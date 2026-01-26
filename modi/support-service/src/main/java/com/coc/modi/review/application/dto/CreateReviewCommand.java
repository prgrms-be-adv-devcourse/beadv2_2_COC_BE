package com.coc.modi.review.application.dto;

public record CreateReviewCommand(

		Long rentalItemId,
		Long memberId,
		Short rating,
		String content
) {
}
