package com.coc.modi.rental.infrastructure.client.dto;

import java.math.BigDecimal;

public record ProductResponseDto(
        Long productId,
        BigDecimal price,
        String status
) {
}
