package com.coc.modi.review.presentation.dto;

import com.coc.modi.review.application.dto.UpdateReviewCommand;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ReviewUpdateRequest(
		@Min(1) @Max(5) Short rating,
		String content
) {

	public UpdateReviewCommand toCommand(Long reviewId, Long memberId) {
		
		return new UpdateReviewCommand(reviewId, memberId, rating, content);
	}
}
