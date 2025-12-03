package com.coc.modi.product.presentation.dto;

import com.coc.modi.product.domain.ProductImageSpec;

import java.math.BigDecimal;
import java.util.List;

public record ProductUpdateRequestDto(
        String name,
        String description,
        BigDecimal pricePerDay,
        String category,
        List<ProductImageSpec> images) {
}
