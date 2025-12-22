package com.coc.modi.product.product.application.dto;

import com.coc.modi.product.product.domain.Product;

public record ProductInternalSellerResponse(
		Long productId,
		String productName,
		String thumbnailImageUrl
) {
	
	public static ProductInternalSellerResponse from(Product product, String thumbnailImageUrl) {
		
		return new ProductInternalSellerResponse(
				product.getId(),
				product.getName(),
				thumbnailImageUrl
		);
	}
}
