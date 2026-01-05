package com.coc.modi.product.recommendation.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;

public record ProductRecommendationRequest(
		@NotBlank(message = "추천 문장은 필수입니다.")
		String query,
		@Min(value = 1, message = "추천 개수는 1 이상이어야 합니다.")
		Integer size
) {
	public int resolvedSize() {
		
		return size != null && size > 0 ? size : 10;
	}
}
