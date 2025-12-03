package com.coc.modi.product.presentation.dto;

import com.coc.modi.product.application.dto.ProductInfo;
import com.coc.modi.product.domain.ProductCategory;
import com.coc.modi.product.domain.ProductStatus;

import java.math.BigDecimal;
import java.util.List;

public record ProductResponseDto(
        Long id,
        String name,
        String description,
        BigDecimal pricePerDay,
        ProductStatus status,
        Long sellerId,
        ProductCategory category,
        Long thumbnailImageId,
        List<ProductImageResponseDto> images
) {

    public static ProductResponseDto from(ProductInfo info) {

        List<ProductImageResponseDto> images = info.images() == null
                ? List.of()
                : info.images().stream().map(ProductImageResponseDto::from).toList();

        return new ProductResponseDto(
                info.id(),
                info.name(),
                info.description(),
                info.pricePerDay(),
                info.status(),
                info.sellerId(),
                info.category(),
                info.thumbnailImageId(),
                images
        );
    }
}
