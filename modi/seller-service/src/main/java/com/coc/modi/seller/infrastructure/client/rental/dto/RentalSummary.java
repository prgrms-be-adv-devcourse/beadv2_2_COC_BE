package com.coc.modi.seller.infrastructure.client.rental.dto;

import java.math.BigDecimal;

public record RentalSummary(
        Long rentalId,
        Long productId,
        String productName,
        Long memberId,
        Long sellerId,
        String status,
        BigDecimal totalAmount,
        String paidAt
) {
}
