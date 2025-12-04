package com.coc.modi.rental.infrastructure.client.dto;

import java.math.BigDecimal;

public record ProductPriceResponseDto(
        Long productId,
        String name,
        BigDecimal price
) {
}
