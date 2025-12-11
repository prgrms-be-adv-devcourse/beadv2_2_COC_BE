package com.coc.modi.review.presentation;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.review.application.ReviewService;
import com.coc.modi.review.application.dto.ReviewResponse;
import com.coc.modi.review.application.dto.ReviewSummaryResponse;
import com.coc.modi.review.presentation.dto.ReviewCreateRequest;
import com.coc.modi.review.presentation.dto.ReviewUpdateRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

	private final ReviewService reviewService;
	
	// 판매자 리뷰 작성
	@PostMapping
	public ResponseEntity<ApiResponse<ReviewResponse>> createReview(Authentication authentication,
																	@Valid @RequestBody ReviewCreateRequest request) {

		Long memberId = (Long) authentication.getPrincipal();
		ReviewResponse response = reviewService.createReview(request.toCommand(memberId));

		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
	}
	
	// 리뷰 수정
	@PatchMapping("/{reviewId}")
	public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(Authentication authentication,
																	@PathVariable Long reviewId,
																	@Valid @RequestBody ReviewUpdateRequest request) {

		Long memberId = (Long) authentication.getPrincipal();
		ReviewResponse response = reviewService.updateReview(request.toCommand(reviewId, memberId));

		return ResponseEntity.ok(ApiResponse.ok(response));
	}
	
	// 리뷰 삭제(소프트 삭제)
	@DeleteMapping("/{reviewId}")
	public ResponseEntity<Void> deleteReview(Authentication authentication,
											 @PathVariable Long reviewId) {

		Long memberId = (Long) authentication.getPrincipal();
		reviewService.deleteReview(reviewId, memberId);

		return ResponseEntity.noContent().build();
	}
	
	// 리뷰 상세 조회
	@GetMapping("/{reviewId}")
	public ResponseEntity<ApiResponse<ReviewResponse>> getReview(@PathVariable Long reviewId) {

		return ResponseEntity.ok(ApiResponse.ok(reviewService.getReview(reviewId)));
	}
	
	// 판매자 리뷰 목록 조회
	@GetMapping
	public ResponseEntity<ApiResponse<List<ReviewSummaryResponse>>> getReviewsBySeller(
			@RequestParam Long sellerId,
			@PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

		List<ReviewSummaryResponse> responses = reviewService.getReviewsBySeller(sellerId, pageable);

		return ResponseEntity.ok(ApiResponse.ok(responses));
	}
	
	// 내가 작성한 리뷰 목록 조회
	@GetMapping("/me")
	public ResponseEntity<ApiResponse<List<ReviewSummaryResponse>>> getMyReviews(Authentication authentication,
																				 @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

		Long memberId = (Long) authentication.getPrincipal();
		List<ReviewSummaryResponse> responses = reviewService.getReviewsByMember(memberId, pageable);

		return ResponseEntity.ok(ApiResponse.ok(responses));
	}
}
