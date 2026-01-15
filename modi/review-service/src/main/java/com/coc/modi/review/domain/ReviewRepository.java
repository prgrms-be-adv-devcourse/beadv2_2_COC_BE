package com.coc.modi.review.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ReviewRepository {

	Review save(Review review);

	Optional<Review> findByIdAndStatus(Long reviewId, ReviewStatus status);

	Page<Review> findBySellerIdAndStatus(Long sellerId, ReviewStatus status, Pageable pageable);

	Page<Review> findByMemberIdAndStatus(Long memberId, ReviewStatus status, Pageable pageable);

	Page<Review> findBySellerIdAndStatusAndIdGreaterThan(Long sellerId, ReviewStatus status, Long reviewId, Pageable pageable);

	long countBySellerIdAndStatus(Long sellerId, ReviewStatus status);

	java.util.List<Long> findDistinctSellerIdsByStatus(ReviewStatus status);

	boolean existsByRentalItemIdAndStatus(Long rentalItemId, ReviewStatus status);
}
