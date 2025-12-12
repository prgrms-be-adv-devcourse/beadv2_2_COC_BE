package com.coc.modi.seller.settlement.application.dto;

import com.coc.modi.seller.settlement.domain.SellerSettlement;
import com.coc.modi.seller.settlement.domain.SellerSettlementStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SellerSettlementResponse(
		Long id,
		Long batchId,
		Long sellerId,
		String periodYm,
		BigDecimal totalRentalAmount,
		BigDecimal totalFeeAmount,
		BigDecimal settlementAmount,
		SellerSettlementStatus status,
		LocalDateTime paidAt,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {
	
	public static SellerSettlementResponse from(SellerSettlement settlement) {
		
		return new SellerSettlementResponse(
				settlement.getId(),
				settlement.getBatchId(),
				settlement.getSellerId(),
				settlement.getPeriodYm(),
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
