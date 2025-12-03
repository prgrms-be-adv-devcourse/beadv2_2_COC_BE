package com.coc.modi.product.presentation.dto;

import com.coc.modi.product.application.dto.ProductListInfo;
import com.coc.modi.product.domain.ProductStatus;

import java.math.BigDecimal;

public record ProductListResponseDto(
        Long id,
        String name,
        BigDecimal pricePerDay,
        ProductStatus status,
        Long sellerId,
        String thumbnailUrl) {



    public static ProductListResponseDto from(ProductListInfo info) {

        return new ProductListResponseDto(
                info.id(),
                info.name(),
                info.pricePerDay(),
                info.status(),
                info.sellerId(),
                info.thumbnailUrl()
        );
    }
}
