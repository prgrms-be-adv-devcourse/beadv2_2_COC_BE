package com.coc.modi.ai.recommendation.presentation.dto;

import java.util.List;
import java.util.Map;

public record ProductRecommendationResponse(
		String message,
		List<Item> items
) {
	public record Item(
			Long productId,
			String name,
			String category,
			Map<String, String> specs,
			String status,
			Double distance
	) {
	}
}
