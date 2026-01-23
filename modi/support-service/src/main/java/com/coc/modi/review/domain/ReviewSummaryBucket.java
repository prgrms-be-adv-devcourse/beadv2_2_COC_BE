package com.coc.modi.review.domain;

import com.coc.modi.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@Table(
		name = "review_summary_bucket",
		schema = "support",
		indexes = {
				@Index(name = "idx_review_summary_bucket_seller_last", columnList = "seller_id, last_review_id")
		}
)
public class ReviewSummaryBucket extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "seller_id", nullable = false)
	private Long sellerId;

	@Column(name = "review_count", nullable = false)
	private int reviewCount;

	@Column(name = "last_review_id", nullable = false)
	private Long lastReviewId;

	@Column(name = "summary", nullable = false, columnDefinition = "text")
	private String summary;

	@Builder
	private ReviewSummaryBucket(Long sellerId, int reviewCount, Long lastReviewId, String summary) {
		this.sellerId = sellerId;
		this.reviewCount = reviewCount;
		this.lastReviewId = lastReviewId;
		this.summary = summary;
	}

	public static ReviewSummaryBucket create(Long sellerId, int reviewCount, Long lastReviewId, String summary) {
		return ReviewSummaryBucket.builder()
				.sellerId(sellerId)
				.reviewCount(reviewCount)
				.lastReviewId(lastReviewId)
				.summary(summary)
				.build();
	}
}
