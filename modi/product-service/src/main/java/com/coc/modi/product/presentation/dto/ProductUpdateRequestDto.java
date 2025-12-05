package com.coc.modi.product.presentation.dto;

import com.coc.modi.product.application.dto.ProductResponse;

import java.math.BigDecimal;
import java.util.List;

public record ProductUpdateRequestDto(
        String name,
        String description,
        BigDecimal pricePerDay,
        String category,
        List<ProductResponse.ImageInfo> images) {
}
