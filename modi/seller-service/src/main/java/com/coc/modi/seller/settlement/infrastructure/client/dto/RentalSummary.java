package com.coc.modi.seller.settlement.infrastructure.client.dto;

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
