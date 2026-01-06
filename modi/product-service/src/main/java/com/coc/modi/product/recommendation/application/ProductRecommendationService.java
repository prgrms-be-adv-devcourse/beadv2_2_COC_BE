package com.coc.modi.product.recommendation.application;

import java.util.List;
import java.util.StringJoiner;

import org.springframework.stereotype.Service;

import com.coc.modi.ai.embedding.EmbeddingClient;
import com.coc.modi.ai.chat.application.ChatService;
import com.coc.modi.product.recommendation.infrastructure.ProductRecommendationRepository;
import com.coc.modi.product.recommendation.infrastructure.ProductRecommendationRepository.ProductRecommendationRow;
import com.coc.modi.product.recommendation.presentation.dto.ProductRecommendationRequest;
import com.coc.modi.product.recommendation.presentation.dto.ProductRecommendationResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductRecommendationService {
	
	private final EmbeddingClient embeddingClient;
	private final ChatService chatService;
	private final ProductRecommendationRepository recommendationRepository;
	
	public List<ProductRecommendationResponse.Item> recommend(ProductRecommendationRequest request) {
		
		if (request == null || request.query() == null || request.query().isBlank()) {
			return List.of();
		}
		
		List<Double> vector;
		try {
			vector = embeddingClient.embed(request.query());
		} catch (Exception ex) {
			log.warn("상품 추천 임베딩 실패. query={}", request.query(), ex);
			return List.of();
		}
		
		List<ProductRecommendationRow> rows = recommendationRepository.findRecommended(vector, request.resolvedSize());
		return rows.stream()
				.map(row -> new ProductRecommendationResponse.Item(
						row.productId(),
						row.name(),
						row.category(),
						row.pricePerDay(),
						row.thumbnailUrl(),
						row.distance()))
				.toList();
	}
	
	public String buildRecommendationMessage(ProductRecommendationRequest request,
											 List<ProductRecommendationResponse.Item> items) {
		
		if (items == null || items.isEmpty()) {
			return "조건에 맞는 추천 상품을 찾지 못했습니다.";
		}
		
		String prompt = buildPrompt(request.query(), items);
		try {
			return chatService.chat(prompt).content();
		} catch (Exception ex) {
			log.warn("추천 문구 생성 실패. query={}", request.query(), ex);
			return "요청하신 용도에 맞는 추천 상품입니다.";
		}
	}
	
	private String buildPrompt(String query, List<ProductRecommendationResponse.Item> items) {
		
		StringJoiner joiner = new StringJoiner("\n");
		for (int i = 0; i < items.size(); i++) {
			ProductRecommendationResponse.Item item = items.get(i);
			joiner.add((i + 1) + ". " + item.name() + " (productId=" + item.productId() + ")");
		}
		
		return """
				사용자 요청: %s
				
				추천 결과:
				%s
				
				요청 의도에 맞춘 추천 사유를 한 문단으로 간결하게 설명해줘.
				각 상품별 사유는 말하지 말고, 전체 추천 이유를 요약해.
				""".formatted(query, joiner);
	}
}
