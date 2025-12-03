package com.coc.modi.product.presentation.dto;

import com.coc.modi.product.application.dto.ProductInfo;

public record ProductImageResponseDto(
        Long id,
        String url,
        Integer ordering
) {
    public static ProductImageResponseDto from(ProductInfo.ImageInfo image) {
        return new ProductImageResponseDto(image.id(),  image.url(), image.ordering());
    }
}
