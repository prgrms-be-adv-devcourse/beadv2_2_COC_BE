package com.coc.modi.seller.seller.infrastructure.client.product.dto;

public record ProductSummaryResponse(
        Long productId,
        String productName,
        String thumbnailImageUrl
) {
}
