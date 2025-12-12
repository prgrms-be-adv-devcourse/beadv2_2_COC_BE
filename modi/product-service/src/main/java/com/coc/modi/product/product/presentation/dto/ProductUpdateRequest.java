package com.coc.modi.product.product.presentation.dto;

import com.coc.modi.product.product.application.dto.ProductResponse;

import java.math.BigDecimal;
import java.util.List;

public record ProductUpdateRequest(
        String name,
        String description,
        BigDecimal pricePerDay,
        String category,
        List<ProductResponse.ImageInfo> images) {
}
