package com.coc.modi.product.product.domain;

import com.coc.modi.product.product.application.dto.ProductDetailResponse;

public record ProductImageSpec(
		Long imageId,        // 기존 이미지면 id 포함, 새 이미지면 null
		String url,
		Integer ordering
) {
	public static ProductImageSpec from(ProductDetailResponse.ImageInfo imageInfo) {
		
		return new ProductImageSpec(imageInfo.imageId(), imageInfo.url(), imageInfo.ordering());
	}
}
