package com.coc.modi.review.infrastructure;

import com.coc.modi.review.domain.ReviewSummaryBucket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewSummaryBucketJpaRepository extends JpaRepository<ReviewSummaryBucket, Long> {

	Optional<ReviewSummaryBucket> findTopBySellerIdOrderByLastReviewIdDesc(Long sellerId);

	Page<ReviewSummaryBucket> findBySellerIdOrderByLastReviewIdDesc(Long sellerId, Pageable pageable);
}
