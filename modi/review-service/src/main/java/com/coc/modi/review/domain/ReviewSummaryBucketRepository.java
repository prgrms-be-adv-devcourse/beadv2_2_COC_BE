package com.coc.modi.review.domain;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ReviewSummaryBucketRepository {

	ReviewSummaryBucket save(ReviewSummaryBucket bucket);

	Optional<ReviewSummaryBucket> findLatestBySellerId(Long sellerId);

	List<ReviewSummaryBucket> findLatestBySellerId(Long sellerId, Pageable pageable);
}
