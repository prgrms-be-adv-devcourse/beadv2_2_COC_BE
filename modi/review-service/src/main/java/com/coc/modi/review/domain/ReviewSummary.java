package com.coc.modi.review.domain;

import com.coc.modi.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "review_summary", schema = "review")
public class ReviewSummary extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "seller_id", nullable = false, unique = true)
	private Long sellerId;

	@Column(name = "review_count", nullable = false)
	private long reviewCount;

	@Column(name = "total_review_count", nullable = false)
	private long totalReviewCount;

	@Column(name = "last_bucket_id")
	private Long lastBucketId;

	@Column(name = "summary", columnDefinition = "text")
	private String summary;

	@Builder
	private ReviewSummary(Long sellerId, long reviewCount, long totalReviewCount, Long lastBucketId, String summary) {

		this.sellerId = sellerId;
		this.reviewCount = reviewCount;
		this.totalReviewCount = totalReviewCount;
		this.lastBucketId = lastBucketId;
		this.summary = summary;
	}

	public static ReviewSummary create(Long sellerId, long reviewCount, long totalReviewCount, Long lastBucketId, String summary) {

		return ReviewSummary.builder()
				.sellerId(sellerId)
				.reviewCount(reviewCount)
				.totalReviewCount(totalReviewCount)
				.lastBucketId(lastBucketId)
				.summary(summary)
				.build();
	}

	public static ReviewSummary createCounter(Long sellerId, long totalReviewCount) {

		return ReviewSummary.builder()
				.sellerId(sellerId)
				.reviewCount(0L)
				.totalReviewCount(totalReviewCount)
				.lastBucketId(null)
				.summary(null)
				.build();
	}

	public void updateSummary(String summary, long reviewCount, long totalReviewCount, Long lastBucketId) {

		this.summary = summary;
		this.reviewCount = reviewCount;
		this.totalReviewCount = totalReviewCount;
		this.lastBucketId = lastBucketId;
	}

	public void updateTotalReviewCount(long totalReviewCount) {
		this.totalReviewCount = totalReviewCount;
	}
}
