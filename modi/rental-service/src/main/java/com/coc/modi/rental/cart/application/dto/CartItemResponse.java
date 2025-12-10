package com.coc.modi.rental.cart.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CartItemResponse(
        Long cartItemId,
        Long productId,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal price,
        String status
) {
}
