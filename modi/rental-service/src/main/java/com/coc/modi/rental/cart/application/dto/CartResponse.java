package com.coc.modi.rental.cart.application.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CartResponse(
        List<CartItemResponse> items,
        LocalDateTime updatedAt
) {
}
