package com.coc.modi.review.application;

import com.coc.modi.common.ErrorCode;
import com.coc.modi.kafka.event.NotificationEvent;
import com.coc.modi.review.application.dto.CreateReviewCommand;
import com.coc.modi.review.application.dto.ReviewListResponse;
import com.coc.modi.review.application.dto.ReviewResponse;
import com.coc.modi.review.application.dto.UpdateReviewCommand;
import com.coc.modi.review.domain.Review;
import com.coc.modi.review.domain.ReviewRepository;
import com.coc.modi.review.domain.ReviewStatus;
import com.coc.modi.review.domain.ReviewSummary;
import com.coc.modi.review.domain.ReviewSummaryRepository;
import com.coc.modi.review.event.NotificationEventPublisher;
import com.coc.modi.review.exception.ReviewAccessDeniedException;
import com.coc.modi.review.exception.ReviewException;
import com.coc.modi.review.exception.ReviewNotFoundException;
import com.coc.modi.review.infrastructure.client.RentalClientAdapter;
import com.coc.modi.review.infrastructure.client.SellerClientAdapter;
import com.coc.modi.review.infrastructure.client.dto.RentalItemInfo;
import com.coc.modi.review.cache.ReturnedRentalCache;
import com.coc.modi.review.cache.ReturnedRentalItem;
import com.coc.modi.review.config.ReviewPolicyProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.time.Duration;
import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class ReviewService {

	private final ReviewRepository reviewRepository;
	private final ReviewSummaryRepository reviewSummaryRepository;
	private final ReviewSummaryService reviewSummaryService;
	private final RentalClientAdapter rentalClientAdapter;
	private final SellerClientAdapter sellerClientAdapter;
	private final NotificationEventPublisher notificationEventPublisher;
	private final ReturnedRentalCache returnedRentalCache;
	private final ReviewPolicyProperties reviewPolicyProperties;

	
	// 판매자 리뷰 작성
	@Transactional
	public ReviewResponse createReview(CreateReviewCommand command) {

		validateReviewEligibility(command);

		Review review = Review.create(
				command.rentalItemid(),
				command.sellerId(),
				command.memberId(),
				command.rating(),
				command.content()
		);

		Review saved = reviewRepository.save(review);
		updateTotalReviewCount(saved.getSellerId(), 1L);
		reviewSummaryService.handleReviewCreated(saved.getSellerId());
		Long sellerMemberId = sellerClientAdapter.getSellerMemberId(saved.getSellerId());

		notificationEventPublisher.publish(
				saved.getId(),
				NotificationEvent.of(
						sellerMemberId,
						"REVIEW_CREATED",
						"리뷰가 등록되었습니다",
						"상품에 새로운 리뷰가 등록되었습니다",
						"REVIEW",
						String.valueOf(saved.getId())
				)
		);
		
		return ReviewResponse.from(saved);
	}
	
	// 작성자가 본인 리뷰를 수정
	@Transactional
	public ReviewResponse updateReview(UpdateReviewCommand command) {
		
		Review review = reviewRepository.findByIdAndStatus(command.reviewId(), ReviewStatus.ACTIVE)
				.orElseThrow(() -> new ReviewNotFoundException(command.reviewId()));

		validateOwnership(review, command.memberId());

		review.update(command.rating(), command.content());
		
		return ReviewResponse.from(review);
	}
	
	// 작성자가 본인 리뷰를 소프트 삭제
	@Transactional
	public void deleteReview(Long reviewId, Long memberId) {
		
		Review review = reviewRepository.findByIdAndStatus(reviewId, ReviewStatus.ACTIVE)
				.orElseThrow(() -> new ReviewNotFoundException(reviewId));

		validateOwnership(review, memberId);

		review.delete();
		updateTotalReviewCount(review.getSellerId(), -1L);
	}
	
	// 특정 판매자 리뷰 목록 조회 (삭제된 리뷰 제외)
	@Transactional(readOnly = true)
	public List<ReviewListResponse> getReviewsBySeller(Long sellerId, Pageable pageable) {
		
		Page<Review> reviews = reviewRepository.findBySellerIdAndStatus(sellerId, ReviewStatus.ACTIVE, pageable);

		return reviews.stream()
				.map(ReviewListResponse::from)
				.toList();
	}
	
	// 내가 작성한 리뷰 목록 조회 (삭제된 리뷰 제외)
	@Transactional(readOnly = true)
	public List<ReviewListResponse> getReviewsByMember(Long memberId, Pageable pageable) {
		
		Page<Review> reviews = reviewRepository.findByMemberIdAndStatus(memberId, ReviewStatus.ACTIVE, pageable);

		return reviews.stream()
				.map(ReviewListResponse::from)
				.toList();
	}

	private void validateOwnership(Review review, Long memberId) {
		
		if (!review.getMemberId().equals(memberId)) {
			
			throw new ReviewAccessDeniedException();
		}
	}

	private void validateReviewEligibility(CreateReviewCommand command) {
		ReturnedRentalItem cached = returnedRentalCache.find(command.rentalItemid()).orElse(null);
		if (cached != null) {
			validateReturnedRental(command, cached.memberId(), cached.sellerId(), cached.status(), cached.returnedAt());
		} else {
			RentalItemInfo rentalItem = rentalClientAdapter.getRentalItem(command.rentalItemid());

			if (rentalItem == null) {
				throw new ReviewException(ErrorCode.RENTAL_ITEM_NOT_FOUND, "대여 상품 정보를 찾을 수 없습니다.");
			}

			validateReturnedRental(command, rentalItem.memberId(), rentalItem.sellerId(), rentalItem.status(), rentalItem.returnedAt());

			if ("RETURNED".equals(rentalItem.status())) {
				returnedRentalCache.save(new ReturnedRentalItem(
						rentalItem.rentalItemId(),
						rentalItem.memberId(),
						rentalItem.sellerId(),
						rentalItem.productId(),
						rentalItem.status(),
						rentalItem.returnedAt()
				));
			}
		}

		if (reviewRepository.existsByRentalItemIdAndStatus(command.rentalItemid(), ReviewStatus.ACTIVE)) {
			throw new ReviewException(ErrorCode.CONFLICT, "이미 리뷰가 작성된 상품입니다.");
		}
	}

	private void validateReturnedRental(CreateReviewCommand command,
										Long memberId,
										Long sellerId,
										String status,
										LocalDateTime returnedAt) {
		if (!command.memberId().equals(memberId)) {
			throw new ReviewException(ErrorCode.REVIEW_FORBIDDEN, "대여자만 리뷰를 작성할 수 있습니다.");
		}

		if (!command.sellerId().equals(sellerId)) {
			throw new ReviewException(ErrorCode.INVALID_INPUT, "판매자 정보가 일치하지 않습니다.");
		}

		if (!"RETURNED".equals(status)) {
			throw new ReviewException(ErrorCode.CONFLICT, "반납 완료된 상품만 리뷰를 작성할 수 있습니다.");
		}

		if (!isWithinReviewWindow(returnedAt)) {
			throw new ReviewException(ErrorCode.CONFLICT, "리뷰 작성 가능 기간이 지났습니다.");
		}
	}

	private boolean isWithinReviewWindow(LocalDateTime returnedAt) {
		if (returnedAt == null) {
			return false;
		}
		Duration reviewableWindow = reviewPolicyProperties.reviewableWindow();
		LocalDateTime deadline = returnedAt.plus(reviewableWindow);
		return !LocalDateTime.now().isAfter(deadline);
	}
	private void updateTotalReviewCount(Long sellerId, long delta) {
		if (delta == 0) {
			return;
		}

		reviewSummaryRepository.findBySellerId(sellerId)
				.ifPresentOrElse(
						summary -> summary.updateTotalReviewCount(Math.max(0, summary.getTotalReviewCount() + delta)),
						() -> {
							if (delta > 0) {
								reviewSummaryRepository.save(ReviewSummary.createCounter(sellerId, delta));
							}
						}
				);
	}
}
