package com.coc.modi.review.domain;

import com.coc.modi.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "review", schema = "review")
public class Review extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "rental_item_id", nullable = false)
	private Long rentalItemId;

	@Column(name = "seller_id", nullable = false)
	private Long sellerId;

	@Column(name = "member_id", nullable = false)
	private Long memberId;

	@Column(name = "rating", nullable = false)
	private Short rating;

	@Column(name = "content", nullable = false, columnDefinition = "text")
	private String content;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20, columnDefinition = "varchar(20) default 'ACTIVE'")
	private ReviewStatus status;

	@Builder
	private Review(Long rentalItemId, Long sellerId, Long memberId, Short rating, String content) {

		validateRating(rating);
		
		this.rentalItemId = rentalItemId;
		this.sellerId = sellerId;
		this.memberId = memberId;
		this.rating = rating;
		this.content = content;
		this.status = ReviewStatus.ACTIVE;
	}

	public static Review create(Long rentalItemId, Long sellerId, Long memberId, Short rating, String content) {

		return Review.builder()
				.rentalItemId(rentalItemId)
				.sellerId(sellerId)
				.memberId(memberId)
				.rating(rating)
				.content(content)
				.build();
	}

	public void update(Short rating, String content) {

		if (rating != null) {
			validateRating(rating);
			this.rating = rating;
		}

		if (content != null) {
			this.content = content;
		}
	}

	private void validateRating(Short rating) {

		if (rating == null || rating < 1 || rating > 5) {
			throw new IllegalArgumentException("리뷰 평점은 1~5 사이여야 합니다.");
		}
	}

	public void delete() {
		
		this.status = ReviewStatus.DELETED;
	}

}
