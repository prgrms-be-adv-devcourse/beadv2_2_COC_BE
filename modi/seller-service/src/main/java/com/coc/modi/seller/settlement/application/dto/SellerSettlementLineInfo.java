package com.coc.modi.seller.settlement.application.dto;

import com.coc.modi.seller.settlement.domain.SellerSettlementLine;

import java.math.BigDecimal;

public record SellerSettlementLineInfo(
        Long id,
        Long sellerSettlementId,
        Long sellerId,
        Long rentalId,
        Long memberId,
        Long productId,
        BigDecimal rentalAmount,
        BigDecimal feeAmount
) {

    public static SellerSettlementLineInfo from(SellerSettlementLine line) {
        return new SellerSettlementLineInfo(
                line.getId(),
                line.getSellerSettlement() != null ? line.getSellerSettlement().getId() : null,
                line.getSellerId(),
                line.getRentalId(),
                line.getMemberId(),
                line.getProductId(),
                line.getRentalAmount(),
                line.getFeeAmount()
        );
    }
}
