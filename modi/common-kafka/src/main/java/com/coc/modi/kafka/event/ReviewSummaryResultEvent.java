package com.coc.modi.kafka.event;

import java.time.Instant;
import java.util.UUID;

public record ReviewSummaryResultEvent(
		String eventId,
		Instant occurredAt,
		Long sellerId,
		String targetType,
		Long lastReviewId,
		Long lastBucketId,
		int reviewCount,
		long totalCount,
		String summary
) {

	public static ReviewSummaryResultEvent forBucket(
			Long sellerId,
			Long lastReviewId,
			int reviewCount,
			String summary
	) {
		return new ReviewSummaryResultEvent(
				UUID.randomUUID().toString(),
				Instant.now(),
				sellerId,
				"BUCKET",
				lastReviewId,
				null,
				reviewCount,
				0L,
				summary
		);
	}

	public static ReviewSummaryResultEvent forFinal(
			Long sellerId,
			Long lastBucketId,
			int reviewCount,
			long totalCount,
			String summary
	) {
		return new ReviewSummaryResultEvent(
				UUID.randomUUID().toString(),
				Instant.now(),
				sellerId,
				"FINAL",
				null,
				lastBucketId,
				reviewCount,
				totalCount,
				summary
		);
	}
}
