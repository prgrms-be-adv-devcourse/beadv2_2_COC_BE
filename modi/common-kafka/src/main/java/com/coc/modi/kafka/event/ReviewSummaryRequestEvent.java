package com.coc.modi.kafka.event;

import java.time.Instant;
import java.util.UUID;

public record ReviewSummaryRequestEvent(
		String eventId,
		Instant occurredAt,
		Long sellerId,
		String targetType,
		Long lastReviewId,
		Long lastBucketId,
		int reviewCount,
		long totalCount,
		int maxLength,
		String prompt,
		String payload
) {

	public static ReviewSummaryRequestEvent forBucket(
			Long sellerId,
			Long lastReviewId,
			int reviewCount,
			int maxLength,
			String prompt,
			String payload
	) {
		return new ReviewSummaryRequestEvent(
				UUID.randomUUID().toString(),
				Instant.now(),
				sellerId,
				"BUCKET",
				lastReviewId,
				null,
				reviewCount,
				0L,
				maxLength,
				prompt,
				payload
		);
	}

	public static ReviewSummaryRequestEvent forFinal(
			Long sellerId,
			Long lastBucketId,
			int reviewCount,
			long totalCount,
			int maxLength,
			String prompt,
			String payload
	) {
		return new ReviewSummaryRequestEvent(
				UUID.randomUUID().toString(),
				Instant.now(),
				sellerId,
				"FINAL",
				null,
				lastBucketId,
				reviewCount,
				totalCount,
				maxLength,
				prompt,
				payload
		);
	}
}
