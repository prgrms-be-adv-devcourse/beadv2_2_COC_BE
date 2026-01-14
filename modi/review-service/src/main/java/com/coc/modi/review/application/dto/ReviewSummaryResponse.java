package com.coc.modi.review.application.dto;

import com.coc.modi.review.domain.ReviewSummary;

import java.time.LocalDateTime;

public record ReviewSummaryResponse(
		Long sellerId,
		String summary,
		long reviewCount,
		LocalDateTime summarizedAt
) {

	public static ReviewSummaryResponse from(ReviewSummary summary) {

		return new ReviewSummaryResponse(
				summary.getSellerId(),
				summary.getSummary(),
				summary.getReviewCount(),
				summary.getUpdatedAt()
		);
	}
}
