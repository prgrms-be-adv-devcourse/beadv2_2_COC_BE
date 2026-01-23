package com.coc.modi.product.product.application.dto;

import com.coc.modi.product.product.domain.Product;
import com.coc.modi.product.product.domain.ProductStatus;

import java.math.BigDecimal;

public record ProductListResponse(
		Long productId,
		String name,
		BigDecimal pricePerDay,
		ProductStatus status,
		Long sellerId,
		String thumbnailUrl
) {
	public static ProductListResponse fromProduct(Product product, String thumbnailUrl) {
		
		return new ProductListResponse(
				product.getId(),
				product.getName(),
				product.getPricePerDay(),
				product.getStatus(),
				product.getSellerId(),
				thumbnailUrl
		);
	}
}
