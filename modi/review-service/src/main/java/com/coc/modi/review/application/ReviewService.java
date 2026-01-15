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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;


@Service
@RequiredArgsConstructor
public class ReviewService {

	private final ReviewRepository reviewRepository;
	private final ReviewSummaryRepository reviewSummaryRepository;
	private final ReviewSummaryService reviewSummaryService;
	private final RentalClientAdapter rentalClientAdapter;
	private final SellerClientAdapter sellerClientAdapter;
	private final NotificationEventPublisher notificationEventPublisher;

	
	// ?먮ℓ??由щ럭 ?앹꽦
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
						"??由щ럭媛 ?깅줉 ?섏뿀?듬땲??",
						"?곹뭹???덈줈??由щ럭媛 ?깅줉?섏뿀?듬땲??",
						"REVIEW",
						String.valueOf(saved.getId())
				)
		);
		
		return ReviewResponse.from(saved);
	}
	
	// ?묒꽦?먭? 蹂몄씤 由щ럭瑜??섏젙
	@Transactional
	public ReviewResponse updateReview(UpdateReviewCommand command) {
		
		Review review = reviewRepository.findByIdAndStatus(command.reviewId(), ReviewStatus.ACTIVE)
				.orElseThrow(() -> new ReviewNotFoundException(command.reviewId()));

		validateOwnership(review, command.memberId());

		review.update(command.rating(), command.content());
		
		return ReviewResponse.from(review);
	}
	
	// ?묒꽦?먭? 蹂몄씤 由щ럭瑜??뚰봽????젣
	@Transactional
	public void deleteReview(Long reviewId, Long memberId) {
		
		Review review = reviewRepository.findByIdAndStatus(reviewId, ReviewStatus.ACTIVE)
				.orElseThrow(() -> new ReviewNotFoundException(reviewId));

		validateOwnership(review, memberId);

		review.delete();
		updateTotalReviewCount(review.getSellerId(), -1L);
	}
	
	// ?뱀젙 ?먮ℓ??由щ럭 紐⑸줉 議고쉶 (??젣??由щ럭 ?쒖쇅)
	@Transactional(readOnly = true)
	public List<ReviewListResponse> getReviewsBySeller(Long sellerId, Pageable pageable) {
		
		Page<Review> reviews = reviewRepository.findBySellerIdAndStatus(sellerId, ReviewStatus.ACTIVE, pageable);

		return reviews.stream()
				.map(ReviewListResponse::from)
				.toList();
	}
	
	// ?닿? ?묒꽦??由щ럭 紐⑸줉 議고쉶 (??젣??由щ럭 ?쒖쇅)
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
		RentalItemInfo rentalItem = rentalClientAdapter.getRentalItem(command.rentalItemid());

		if (rentalItem == null) {
			throw new ReviewException(ErrorCode.RENTAL_ITEM_NOT_FOUND, "????꾩씠???뺣낫瑜?李얠쓣 ???놁뒿?덈떎.");
		}

		if (!command.memberId().equals(rentalItem.memberId())) {
			throw new ReviewException(ErrorCode.REVIEW_FORBIDDEN, "??ъ옄留?由щ럭瑜??묒꽦?????덉뒿?덈떎.");
		}

		if (!command.sellerId().equals(rentalItem.sellerId())) {
			throw new ReviewException(ErrorCode.INVALID_INPUT, "?먮ℓ???뺣낫媛 ?쇱튂?섏? ?딆뒿?덈떎.");
		}

		if (!"RETURNED".equals(rentalItem.status())) {
			throw new ReviewException(ErrorCode.CONFLICT, "諛섎궔 ?꾨즺???곹뭹留?由щ럭瑜??묒꽦?????덉뒿?덈떎.");
		}

		if (reviewRepository.existsByRentalItemIdAndStatus(command.rentalItemid(), ReviewStatus.ACTIVE)) {
			throw new ReviewException(ErrorCode.CONFLICT, "?대? 由щ럭媛 ?묒꽦??????곹뭹?낅땲??");
		}
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
