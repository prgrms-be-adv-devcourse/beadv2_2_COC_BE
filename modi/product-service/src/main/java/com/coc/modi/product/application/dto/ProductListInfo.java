package com.coc.modi.product.application.dto;

import com.coc.modi.product.domain.Product;
import com.coc.modi.product.domain.ProductStatus;

import java.math.BigDecimal;

public record ProductListInfo(
        Long id,
        String name,
        BigDecimal pricePerDay,
        ProductStatus status,
        Long sellerId,
        String thumbnailUrl
) {
    public static ProductListInfo of(Product product, String thumbnailUrl) {
        return new ProductListInfo(
                product.getId(),
                product.getName(),
                product.getPricePerDay(),
                product.getStatus(),
                product.getSellerId(),
                thumbnailUrl
        );
    }
}
