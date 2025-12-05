package com.coc.modi.product.presentation.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductRequestDto(
        String name,
        String description,
        BigDecimal pricePerDay,
        String category,
        List<String> images
) {
}
