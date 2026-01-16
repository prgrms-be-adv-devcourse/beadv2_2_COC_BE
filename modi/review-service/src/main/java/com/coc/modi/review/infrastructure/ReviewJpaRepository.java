package com.coc.modi.review.infrastructure;

import com.coc.modi.review.domain.Review;
import com.coc.modi.review.domain.ReviewStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface ReviewJpaRepository extends JpaRepository<Review, Long> {

	Optional<Review> findByIdAndStatus(Long reviewId, ReviewStatus status);

	Page<Review> findBySellerIdAndStatus(Long sellerId, ReviewStatus status, Pageable pageable);

	Page<Review> findBySellerIdAndStatusAndRating(Long sellerId, ReviewStatus status, Short rating, Pageable pageable);

	Page<Review> findByMemberIdAndStatus(Long memberId, ReviewStatus status, Pageable pageable);

	Page<Review> findByMemberIdAndStatusAndRating(Long memberId, ReviewStatus status, Short rating, Pageable pageable);

	Page<Review> findBySellerIdAndStatusAndIdGreaterThan(Long sellerId, ReviewStatus status, Long reviewId, Pageable pageable);

	long countBySellerIdAndStatus(Long sellerId, ReviewStatus status);

	boolean existsByRentalItemIdAndStatus(Long rentalItemId, ReviewStatus status);

	@Query("select coalesce(sum(r.rating), 0) from Review r where r.sellerId = :sellerId and r.status = :status")
	long sumRatingBySellerIdAndStatus(@Param("sellerId") Long sellerId, @Param("status") ReviewStatus status);

	@Query("select distinct r.sellerId from Review r where r.status = :status")
	List<Long> findDistinctSellerIdsByStatus(@Param("status") ReviewStatus status);
}
