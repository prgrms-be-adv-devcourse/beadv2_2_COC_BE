package com.coc.modi.review.infrastructure;

import com.coc.modi.review.domain.Review;
import com.coc.modi.review.domain.ReviewStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewJpaRepository extends JpaRepository<Review, Long> {

	Optional<Review> findByIdAndStatus(Long reviewId, ReviewStatus status);

	Page<Review> findBySellerIdAndStatus(Long sellerId, ReviewStatus status, Pageable pageable);

	Page<Review> findByMemberIdAndStatus(Long memberId, ReviewStatus status, Pageable pageable);
}
