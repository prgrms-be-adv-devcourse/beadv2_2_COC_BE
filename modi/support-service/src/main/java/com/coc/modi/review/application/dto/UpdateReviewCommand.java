package com.coc.modi.review.application.dto;

public record UpdateReviewCommand(

		Long reviewId,
		Long memberId,
		Short rating,
		String content
) {
}
