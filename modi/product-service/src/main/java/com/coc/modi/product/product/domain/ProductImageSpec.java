package com.coc.modi.product.product.domain;

import com.coc.modi.product.product.application.dto.ProductResponse;

public record ProductImageSpec(
		Long id,        // 기존 이미지면 id 포함, 새 이미지면 null
		String url,
		Integer ordering
) {
	public static ProductImageSpec from(ProductResponse.ImageInfo imageInfo) {
		
		return new ProductImageSpec(imageInfo.id(), imageInfo.url(), imageInfo.ordering());
	}
}
