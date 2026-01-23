package com.coc.modi.ai.recommendation.presentation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

public record ProductRecommendationRequest(
		@Positive(message = "상품 ID는 1 이상이어야 합니다.")
		Long productId,
		String query,
		java.util.List<String> categories,
		@Min(value = 1, message = "추천 개수는 1 이상이어야 합니다.")
		Integer size
) {
	public int resolvedSize() {
		
		return size != null && size > 0 ? size : 10;
	}

	public boolean hasProductId() {
		
		return productId != null && productId > 0;
	}

	public boolean hasQuery() {
		
		return query != null && !query.isBlank();
	}

	public boolean hasCategories() {
		
		return categories != null && !categories.isEmpty();
	}
}
