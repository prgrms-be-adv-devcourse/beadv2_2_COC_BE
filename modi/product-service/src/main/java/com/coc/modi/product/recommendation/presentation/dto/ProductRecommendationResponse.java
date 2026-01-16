package com.coc.modi.product.recommendation.presentation.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductRecommendationResponse(
		String message,
		List<Item> items
) {
	public record Item(
			Long productId,
			String name,
			String category,
			BigDecimal pricePerDay,
			String thumbnailUrl,
			Double distance
	) {
	}
}
