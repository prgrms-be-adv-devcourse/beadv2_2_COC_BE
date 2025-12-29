package com.coc.modi.review.application.dto;

import com.coc.modi.review.domain.Review;

import java.time.LocalDateTime;

public record ReviewResponse(

		Long reviewId,
		Long rentalItemId,
		Long sellerId,
		Long memberId,
		Short rating,
		String content,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {

	public static ReviewResponse from(Review review) {
		
		return new ReviewResponse(

				review.getReviewId(),
				review.getRentalItemId(),
				review.getSellerId(),
				review.getMemberId(),
				review.getRating(),
				review.getContent(),
				review.getCreatedAt(),
				review.getUpdatedAt()
		);
	}
}
