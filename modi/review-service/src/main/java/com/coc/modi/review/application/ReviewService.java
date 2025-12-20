package com.coc.modi.review.application;

import com.coc.modi.review.application.dto.CreateReviewCommand;
import com.coc.modi.review.application.dto.ReviewResponse;
import com.coc.modi.review.application.dto.ReviewSummaryResponse;
import com.coc.modi.review.application.dto.UpdateReviewCommand;
import com.coc.modi.review.domain.Review;
import com.coc.modi.review.domain.ReviewRepository;
import com.coc.modi.review.domain.ReviewStatus;
import com.coc.modi.review.event.NotificationEventPublisher;
import com.coc.modi.review.exception.ReviewAccessDeniedException;
import com.coc.modi.review.exception.ReviewNotFoundException;
import com.coc.modi.kafka.event.NotificationEvent;
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
	private final NotificationEventPublisher notificationEventPublisher;
	private final ReviewSummaryService reviewSummaryService;

	
	// 판매자 리뷰 생성
	@Transactional
	public ReviewResponse createReview(CreateReviewCommand command) {

		String summary = reviewSummaryService.summarize(command.content());

		Review review = Review.create(
				command.rentalItemid(),
				command.sellerId(),
				command.memberId(),
				command.rating(),
				command.content(),
				summary
		);

		Review saved = reviewRepository.save(review);

		// notificationEventPublisher.publish(
		// 		NotificationEvent.of(
		// 				saved.getSellerId(),
		// 				"REVIEW_CREATED",
		// 				"새 리뷰가 등록 되었습니다!",
		// 				"상품에 새로운 리뷰가 등록되었습니다.",
		// 				"REVIEW",
		// 				String.valueOf(saved.getId())
		// 		)
		// );
		
		return ReviewResponse.from(saved);
	}
	
	// 작성자가 본인 리뷰를 수정
	@Transactional
	public ReviewResponse updateReview(UpdateReviewCommand command) {
		
		Review review = reviewRepository.findByIdAndStatus(command.reviewId(), ReviewStatus.ACTIVE)
				.orElseThrow(() -> new ReviewNotFoundException(command.reviewId()));

		validateOwnership(review, command.memberId());

		String summary = command.content() != null ? reviewSummaryService.summarize(command.content()) : null;
		review.update(command.rating(), command.content(), summary);
		
		return ReviewResponse.from(review);
	}
	
	// 작성자가 본인 리뷰를 소프트 삭제
	@Transactional
	public void deleteReview(Long reviewId, Long memberId) {
		
		Review review = reviewRepository.findByIdAndStatus(reviewId, ReviewStatus.ACTIVE)
				.orElseThrow(() -> new ReviewNotFoundException(reviewId));

		validateOwnership(review, memberId);

		review.delete();
	}
	
	// 단건 리뷰 조회 (삭제된 리뷰 제외)
	@Transactional(readOnly = true)
	public ReviewResponse getReview(Long reviewId) {
		
		return reviewRepository.findByIdAndStatus(reviewId, ReviewStatus.ACTIVE)
				.map(ReviewResponse::from)
				.orElseThrow(() -> new ReviewNotFoundException(reviewId));
	}
	
	// 특정 판매자 리뷰 목록 조회 (삭제된 리뷰 제외)
	@Transactional(readOnly = true)
	public List<ReviewSummaryResponse> getReviewsBySeller(Long sellerId, Pageable pageable) {
		
		Page<Review> reviews = reviewRepository.findBySellerIdAndStatus(sellerId, ReviewStatus.ACTIVE, pageable);

		return reviews.stream()
				.map(ReviewSummaryResponse::from)
				.toList();
	}
	
	// 내가 작성한 리뷰 목록 조회 (삭제된 리뷰 제외)
	@Transactional(readOnly = true)
	public List<ReviewSummaryResponse> getReviewsByMember(Long memberId, Pageable pageable) {
		
		Page<Review> reviews = reviewRepository.findByMemberIdAndStatus(memberId, ReviewStatus.ACTIVE, pageable);

		return reviews.stream()
				.map(ReviewSummaryResponse::from)
				.toList();
	}

	private void validateOwnership(Review review, Long memberId) {
		
		if (!review.getMemberId().equals(memberId)) {
			
			throw new ReviewAccessDeniedException();
		}
	}
}
