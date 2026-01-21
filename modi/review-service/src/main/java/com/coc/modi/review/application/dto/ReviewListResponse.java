package com.coc.modi.review.application.dto;

import com.coc.modi.review.domain.Review;

import java.time.LocalDateTime;

public record ReviewListResponse(
		Long reviewId,
		Long rentalItemId,
		Long sellerId,
		Long memberId,
		Short rating,
		String content,
		LocalDateTime createdAt
) {

	public static ReviewListResponse from(Review review) {

		return new ReviewListResponse(
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
