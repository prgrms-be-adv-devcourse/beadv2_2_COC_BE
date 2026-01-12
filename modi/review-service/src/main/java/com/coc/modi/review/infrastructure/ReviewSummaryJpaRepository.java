package com.coc.modi.review.infrastructure;

import com.coc.modi.review.domain.ReviewSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewSummaryJpaRepository extends JpaRepository<ReviewSummary, Long> {

	Optional<ReviewSummary> findBySellerId(Long sellerId);
}
