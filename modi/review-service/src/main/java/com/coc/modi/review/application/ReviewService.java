package com.coc.modi.review.application;

import com.coc.modi.common.NotificationChannel;
import com.coc.modi.common.NotificationEvent;
import com.coc.modi.common.NotificationType;
import com.coc.modi.common.ReviewCreatedEvent;
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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class ReviewService {

	private final ReviewRepository reviewRepository;
	private final NotificationEventPublisher notificationEventPublisher;

	
	// 판매자 리뷰 생성
	@Transactional
	public ReviewResponse createReview(CreateReviewCommand command) {
		
		Review review = Review.create(
				command.rentalItemid(),
				command.sellerId(),
				command.memberId(),
				command.rating(),
				command.content()
		);

		Review saved = reviewRepository.save(review);
		
		NotificationEvent event = new NotificationEvent();
		event.setType(NotificationType.REVIEW_CREATED);
		event.setReceiverId(saved.getSellerId());
		event.setTitle("새 리뷰가 등록 되었습니다!");
		event.setContent("상품에 새로운 리뷰가 등록되어습니다.");
		event.setReferenceType("REVIEW");
		event.setReferenceId(saved.getId());
		event.setChannels(Set.of(NotificationChannel.IN_APP));
		
		notificationEventPublisher.publish(event);
		
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
