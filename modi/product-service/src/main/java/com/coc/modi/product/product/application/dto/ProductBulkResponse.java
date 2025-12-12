package com.coc.modi.product.product.application.dto;

import com.coc.modi.product.product.domain.Product;

import java.math.BigDecimal;

public record ProductBulkResponse(
        Long productId,
        Long sellerId,
        BigDecimal price,
        String status
) {

    public static ProductBulkResponse from(Product product) {
        return new ProductBulkResponse(
                product.getId(),
                product.getSellerId(),
                product.getPricePerDay(),
                product.getStatus().name()
        );
    }
}
