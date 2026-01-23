package com.coc.modi.review.infrastructure;

import com.coc.modi.review.domain.ReviewSummary;
import com.coc.modi.review.domain.ReviewSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReviewSummaryRepositoryAdapter implements ReviewSummaryRepository {

	private final ReviewSummaryJpaRepository reviewSummaryJpaRepository;

	@Override
	public ReviewSummary save(ReviewSummary summary) {
		return reviewSummaryJpaRepository.save(summary);
	}

	@Override
	public Optional<ReviewSummary> findBySellerId(Long sellerId) {
		return reviewSummaryJpaRepository.findBySellerId(sellerId);
	}

	@Override
	public void delete(ReviewSummary summary) {
		reviewSummaryJpaRepository.delete(summary);
	}
}
