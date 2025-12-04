package com.coc.modi.product.application.dto;

import com.coc.modi.product.domain.ProductStatus;
import com.coc.modi.product.search.ProductDocument;

import java.math.BigDecimal;

public record ProductListResponse(
        Long id,
        String name,
        BigDecimal pricePerDay,
        ProductStatus status,
        Long sellerId,
        String thumbnailUrl
) {
    public static ProductListResponse from(ProductDocument product) {
        return new ProductListResponse(
                product.getId(),
                product.getName(),
                product.getPricePerDay(),
                ProductStatus.valueOf(product.getStatus()),
                product.getSellerId(),
                product.getThumbnailUrl()
        );
    }
}
