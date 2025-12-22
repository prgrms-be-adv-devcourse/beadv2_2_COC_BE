package com.coc.modi.seller.seller.infrastructure.client.rental.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record RentalItemInfo(
        Long rentalItemId,
        Long productId,
        Long memberId,
        Long sellerId,
        String status,
        BigDecimal totalAmount,
        LocalDate startDate,
        LocalDate endDate,
        LocalDateTime paidAt
) {
}
