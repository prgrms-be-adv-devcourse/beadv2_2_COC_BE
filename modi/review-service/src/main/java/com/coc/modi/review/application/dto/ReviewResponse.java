package com.coc.modi.review.application.dto;

import com.coc.modi.review.domain.Review;

import java.time.LocalDateTime;

public record ReviewResponse(
		Long id,
		Long rentalId,
		Long sellerId,
		Long memberId,
		Short rating,
		String content,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {

	public static ReviewResponse from(Review review) {
		
		return new ReviewResponse(
				review.getId(),
				review.getRentalId(),
				review.getSellerId(),
				review.getMemberId(),
				review.getRating(),
				review.getContent(),
				review.getCreatedAt(),
				review.getUpdatedAt()
		);
	}
}
