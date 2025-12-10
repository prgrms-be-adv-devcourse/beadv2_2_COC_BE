package com.coc.modi.rental.cart.application.dto;

import java.time.LocalDate;

public record UpdateCartItemCommand(
        Long memberId,
        Long cartItemId,
        LocalDate startDate,
        LocalDate endDate
) {
}
