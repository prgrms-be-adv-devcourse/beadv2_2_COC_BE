package com.coc.modi.product.product.application.dto;

import java.math.BigDecimal;
import java.util.List;

import com.coc.modi.product.product.domain.ProductCategory;
import com.coc.modi.product.product.presentation.dto.ProductCreateRequest;

public record ProductCreateCommand(
		Long memberId,
		String name,
		String description,
		BigDecimal pricePerDay,
		ProductCategory category,
		List<String> imageUrls
) {
	
	public static ProductCreateCommand toCommand(Long memberId, ProductCreateRequest productCreateRequest) {
		
		return new ProductCreateCommand(
				memberId,
				productCreateRequest.name(),
				productCreateRequest.description(),
				productCreateRequest.pricePerDay(),
				productCreateRequest.category(),
				productCreateRequest.images());
	}
}