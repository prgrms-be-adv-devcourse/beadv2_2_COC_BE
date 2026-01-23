package com.coc.modi.product.product.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.coc.modi.product.product.domain.ProductCategory;
import com.coc.modi.product.product.presentation.dto.ProductUpdateRequest;

public record ProductUpdateCommand(
		Long memberId,
		Long productId,
		String name,
		String description,
		BigDecimal pricePerDay,
		BigDecimal securityDepositAmount,
		ProductCategory category,
		Map<String, String> specs,
		List<ProductDetailResponse.ImageInfo> images
) {
	
	public static ProductUpdateCommand toCommand(Long memberId, Long productId, ProductUpdateRequest productUpdateRequest) {
		
		return new ProductUpdateCommand(
				memberId,
				productId,
				productUpdateRequest.name(),
				productUpdateRequest.description(),
				productUpdateRequest.pricePerDay(),
				productUpdateRequest.securityDepositAmount(),
				productUpdateRequest.category(),
				productUpdateRequest.specs(),
				productUpdateRequest.images()
		);
	}
}
