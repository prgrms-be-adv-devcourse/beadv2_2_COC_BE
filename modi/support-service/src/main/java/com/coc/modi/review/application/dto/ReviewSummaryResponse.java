package com.coc.modi.review.application.dto;

import com.coc.modi.review.domain.ReviewSummary;

import java.time.LocalDateTime;

public record ReviewSummaryResponse(
		Long sellerId,
		String summary,
		long reviewCount,
		long totalReviewCount,
		long ratingSum,
		double averageRating,
		LocalDateTime summarizedAt
) {

	public static ReviewSummaryResponse from(ReviewSummary summary) {
		long totalReviewCount = summary.getTotalReviewCount();
		long ratingSum = summary.getRatingSum();
		double averageRating = totalReviewCount > 0 ? (double) ratingSum / totalReviewCount : 0.0;

		return new ReviewSummaryResponse(
				summary.getSellerId(),
				summary.getSummary(),
				summary.getReviewCount(),
				totalReviewCount,
				ratingSum,
				averageRating,
				summary.getUpdatedAt()
		);
	}
}
