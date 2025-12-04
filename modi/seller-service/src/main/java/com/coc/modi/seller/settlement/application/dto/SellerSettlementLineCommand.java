package com.coc.modi.seller.settlement.application.dto;

import java.math.BigDecimal;

public record SellerSettlementLineCommand(
        Long batchId,
        Long sellerId,
        String periodYm,
        Long rentalId,
        Long memberId,
        Long productId,
        BigDecimal rentalAmount,
        BigDecimal feeAmount
) {
}
