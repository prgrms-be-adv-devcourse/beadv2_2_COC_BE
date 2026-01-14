package com.coc.modi.product.product.presentation.internal.dto;

import java.util.Map;

import com.coc.modi.product.product.domain.Product;

public record ProductEmbeddingResponse(
        Long productId,
        String name,
        String description,
        String category,
        Map<String, String> specs,
        String status
) {
    public static ProductEmbeddingResponse from(Product product) {

        return new ProductEmbeddingResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getCategory() != null ? product.getCategory().name() : null,
                product.getSpecs(),
                product.getStatus() != null ? product.getStatus().name() : null
        );
    }
}
