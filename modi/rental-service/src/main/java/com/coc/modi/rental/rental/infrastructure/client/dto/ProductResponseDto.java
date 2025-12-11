package com.coc.modi.rental.rental.infrastructure.client.dto;

import java.math.BigDecimal;

public record ProductResponseDto(
		Long productId,
		Long sellerId,
		BigDecimal price,
		String status
) {
}
