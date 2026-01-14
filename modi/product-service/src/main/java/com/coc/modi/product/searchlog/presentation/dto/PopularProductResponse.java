package com.coc.modi.product.searchlog.presentation.dto;

public record PopularProductResponse(
		Long productId,
		String productName,
		Long count
) {
}
