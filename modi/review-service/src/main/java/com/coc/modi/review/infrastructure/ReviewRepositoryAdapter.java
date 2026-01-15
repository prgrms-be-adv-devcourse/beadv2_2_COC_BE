package com.coc.modi.review.infrastructure;

import com.coc.modi.review.domain.Review;
import com.coc.modi.review.domain.ReviewRepository;
import com.coc.modi.review.domain.ReviewStatus;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryAdapter implements ReviewRepository {

	private final ReviewJpaRepository reviewJpaRepository;

	@Override
	public Review save(Review review) {
		return reviewJpaRepository.save(review);
	}

	@Override
	public Optional<Review> findByIdAndStatus(Long reviewId, ReviewStatus status) {
		
		return reviewJpaRepository.findByIdAndStatus(reviewId, status);
	}

	@Override
	public Page<Review> findBySellerIdAndStatus(Long sellerId, ReviewStatus status, Pageable pageable) {
		
		return reviewJpaRepository.findBySellerIdAndStatus(sellerId, status, pageable);
	}

	@Override
	public Page<Review> findByMemberIdAndStatus(Long memberId, ReviewStatus status, Pageable pageable) {
		
		return reviewJpaRepository.findByMemberIdAndStatus(memberId, status, pageable);
	}

	@Override
	public Page<Review> findBySellerIdAndStatusAndIdGreaterThan(Long sellerId, ReviewStatus status, Long reviewId, Pageable pageable) {
		
		return reviewJpaRepository.findBySellerIdAndStatusAndIdGreaterThan(sellerId, status, reviewId, pageable);
	}

	@Override
	public long countBySellerIdAndStatus(Long sellerId, ReviewStatus status) {
		
		return reviewJpaRepository.countBySellerIdAndStatus(sellerId, status);
	}

	@Override
	public boolean existsByRentalItemIdAndStatus(Long rentalItemId, ReviewStatus status) {
		
		return reviewJpaRepository.existsByRentalItemIdAndStatus(rentalItemId, status);
	}

	@Override
	public java.util.List<Long> findDistinctSellerIdsByStatus(ReviewStatus status) {

		return reviewJpaRepository.findDistinctSellerIdsByStatus(status);
	}
}
