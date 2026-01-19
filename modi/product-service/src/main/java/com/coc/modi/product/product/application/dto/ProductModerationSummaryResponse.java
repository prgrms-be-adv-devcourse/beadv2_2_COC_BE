package com.coc.modi.product.product.application.dto;

import java.time.LocalDateTime;

import com.coc.modi.product.product.domain.Product;
import com.coc.modi.product.product.domain.ProductModerationStatus;
import com.coc.modi.product.product.domain.ProductStatus;

public record ProductModerationSummaryResponse(
		Long productId,
		String name,
		Long sellerId,
		ProductStatus status,
		ProductModerationStatus moderationStatus,
		LocalDateTime createdAt
) {
	public static ProductModerationSummaryResponse from(Product product) {

		return new ProductModerationSummaryResponse(
				product.getId(),
				product.getName(),
				product.getSellerId(),
				product.getStatus(),
				product.getModerationStatus(),
				product.getCreatedAt()
		);
	}
}
