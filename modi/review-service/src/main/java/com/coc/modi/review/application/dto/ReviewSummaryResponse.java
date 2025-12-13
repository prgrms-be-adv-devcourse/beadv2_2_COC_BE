package com.coc.modi.review.application.dto;

import com.coc.modi.review.domain.Review;

import java.time.LocalDateTime;

public record ReviewSummaryResponse(
		Long id,
		Long rentalItemId,
		Long sellerId,
		Long memberId,
		Short rating,
		String content,
		LocalDateTime createdAt
) {

	public static ReviewSummaryResponse from(Review review) {
		
		return new ReviewSummaryResponse(
				review.getId(),
				review.getRentalItemId(),
				review.getSellerId(),
				review.getMemberId(),
				review.getRating(),
				review.getContent(),
				review.getCreatedAt()
		);
	}
}
