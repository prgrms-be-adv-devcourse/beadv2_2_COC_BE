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

	@Column(name = "summary", nullable = false, columnDefinition = "text")
	private String summary;

	@Builder
	private ReviewSummary(Long sellerId, long reviewCount, String summary) {

		this.sellerId = sellerId;
		this.reviewCount = reviewCount;
		this.summary = summary;
	}

	public static ReviewSummary create(Long sellerId, long reviewCount, String summary) {

		return ReviewSummary.builder()
				.sellerId(sellerId)
				.reviewCount(reviewCount)
				.summary(summary)
				.build();
	}

	public void updateSummary(String summary, long reviewCount) {

		this.summary = summary;
		this.reviewCount = reviewCount;
	}
}
