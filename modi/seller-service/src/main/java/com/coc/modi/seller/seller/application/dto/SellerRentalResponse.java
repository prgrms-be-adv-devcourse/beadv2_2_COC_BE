package com.coc.modi.seller.seller.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record SellerRentalResponse(
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
