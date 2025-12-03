package com.coc.modi.seller.settlement.application.dto;

import com.coc.modi.seller.settlement.domain.SellerSettlement;
import com.coc.modi.seller.settlement.domain.SellerSettlementStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SellerSettlementInfo(
        Long id,
        Long sellerId,
        BigDecimal totalRentalAmount,
        BigDecimal totalFeeAmount,
        BigDecimal settlementAmount,
        SellerSettlementStatus status,
        LocalDateTime paidAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static SellerSettlementInfo from(SellerSettlement settlement) {
        return new SellerSettlementInfo(
                settlement.getId(),
                settlement.getSellerId(),
                settlement.getTotalRentalAmount(),
                settlement.getTotalFeeAmount(),
                settlement.getSettlementAmount(),
                settlement.getStatus(),
                settlement.getPaidAt(),
                settlement.getCreatedAt(),
                settlement.getUpdatedAt()
        );
    }
}
