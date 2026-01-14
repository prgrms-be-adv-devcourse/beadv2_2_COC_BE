package com.coc.modi.product.recommendation.presentation;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.ai.chat.application.ChatService;
import com.coc.modi.product.recommendation.application.ProductRecommendationService;
import com.coc.modi.product.recommendation.presentation.dto.ChatTestRequest;
import com.coc.modi.product.recommendation.presentation.dto.ProductRecommendationRequest;
import com.coc.modi.product.recommendation.presentation.dto.ProductRecommendationResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
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
}
