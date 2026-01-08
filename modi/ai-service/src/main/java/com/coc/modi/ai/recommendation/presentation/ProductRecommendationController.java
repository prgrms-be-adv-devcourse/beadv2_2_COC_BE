package com.coc.modi.ai.recommendation.presentation;

import com.coc.modi.ai.chat.application.ChatService;
import com.coc.modi.ai.recommendation.application.ProductRecommendationService;
import com.coc.modi.ai.recommendation.presentation.dto.ChatTestRequest;
import com.coc.modi.ai.recommendation.presentation.dto.ProductRecommendationRequest;
import com.coc.modi.ai.recommendation.presentation.dto.ProductRecommendationResponse;
import com.coc.modi.common.ApiResponse;
import com.coc.modi.common.auth.CustomMember;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
public class ProductRecommendationController {
	
	private final ProductRecommendationService productRecommendationService;
	private final ChatService chatService;
	
	// 추천 상품 조회
	@PostMapping("/recommendations")
	public ResponseEntity<ApiResponse<ProductRecommendationResponse>> recommendProducts(
			@Valid @RequestBody ProductRecommendationRequest request) {
		
		List<ProductRecommendationResponse.Item> items = productRecommendationService.recommend(request);
		String message = productRecommendationService.buildRecommendationMessage(request, items);
		
		return ResponseEntity.ok(ApiResponse.ok(new ProductRecommendationResponse(message, items)));
	}
	
	// AI 채팅 테스트
	@PostMapping("/ai/chat-test")
	public ResponseEntity<ApiResponse<String>> chatTest(@Valid @RequestBody ChatTestRequest request) {
		
		return ResponseEntity.ok(ApiResponse.ok(chatService.chat(request.message()).content()));
	}

	// 최근 조회 기반 추천 상품 조회 (메시지 생성 제외)
	@GetMapping("/recommendations/recent")
	public ResponseEntity<ApiResponse<List<ProductRecommendationResponse.Item>>> recommendRecentProducts(
			@AuthenticationPrincipal CustomMember member,
			@org.springframework.web.bind.annotation.RequestParam(name = "size", defaultValue = "10") Integer size) {
		
		Long memberId = member != null ? member.memberId() : null;
		List<ProductRecommendationResponse.Item> items = productRecommendationService.recommendRecent(memberId, size);
		
		return ResponseEntity.ok(ApiResponse.ok(items));
	}
}
