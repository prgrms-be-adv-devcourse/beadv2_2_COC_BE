package com.coc.modi.review.application.dto;

import com.coc.modi.review.domain.Review;

import java.time.LocalDateTime;

public record ReviewSummaryResponse(
		Long reviewId,
		Long rentalItemId,
		Long sellerId,
		Long memberId,
		Short rating,
		String summary,
		LocalDateTime createdAt
) {

	public static ReviewSummaryResponse from(Review review) {
		
		return new ReviewSummaryResponse(
				review.getReviewId(),
				review.getRentalItemId(),
				review.getSellerId(),
				review.getMemberId(),
				review.getRating(),
				review.getSummary(),
				review.getCreatedAt()
		);
	}
}
