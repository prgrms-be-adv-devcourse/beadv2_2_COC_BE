package com.coc.modi.review.infrastructure;

import com.coc.modi.review.domain.ReviewSummaryBucket;
import com.coc.modi.review.domain.ReviewSummaryBucketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReviewSummaryBucketRepositoryAdapter implements ReviewSummaryBucketRepository {

	private final ReviewSummaryBucketJpaRepository reviewSummaryBucketJpaRepository;

	@Override
	public ReviewSummaryBucket save(ReviewSummaryBucket bucket) {
		return reviewSummaryBucketJpaRepository.save(bucket);
	}

	@Override
	public Optional<ReviewSummaryBucket> findLatestBySellerId(Long sellerId) {
		return reviewSummaryBucketJpaRepository.findTopBySellerIdOrderByLastReviewIdDesc(sellerId);
	}

	@Override
	public List<ReviewSummaryBucket> findLatestBySellerId(Long sellerId, Pageable pageable) {
		return reviewSummaryBucketJpaRepository.findBySellerIdOrderByLastReviewIdDesc(sellerId, pageable)
				.getContent();
	}

	@Override
	public Optional<ReviewSummaryBucket> findBySellerIdAndLastReviewId(Long sellerId, Long lastReviewId) {
		return reviewSummaryBucketJpaRepository.findBySellerIdAndLastReviewId(sellerId, lastReviewId);
	}
}
