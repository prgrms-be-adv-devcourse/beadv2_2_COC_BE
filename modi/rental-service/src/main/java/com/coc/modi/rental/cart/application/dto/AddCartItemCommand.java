package com.coc.modi.rental.cart.application.dto;

import java.time.LocalDate;

public record AddCartItemCommand(
        Long memberId,
        Long productId,
        LocalDate startDate,
        LocalDate endDate
) {
}
