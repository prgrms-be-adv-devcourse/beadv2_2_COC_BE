package com.coc.modi.review.domain;

import java.util.Optional;

public interface ReviewSummaryRepository {

	ReviewSummary save(ReviewSummary summary);

	Optional<ReviewSummary> findBySellerId(Long sellerId);

	void delete(ReviewSummary summary);
}
