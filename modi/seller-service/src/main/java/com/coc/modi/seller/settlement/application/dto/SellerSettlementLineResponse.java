package com.coc.modi.seller.settlement.application.dto;

import com.coc.modi.seller.settlement.domain.SellerSettlementLine;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SellerSettlementLineResponse(
		Long id,
		Long sellerSettlementId,
		Long sellerId,
		Long rentalItemId,
		Long memberId,
		Long productId,
		BigDecimal rentalAmount,
		BigDecimal feeAmount,
		String status,
		LocalDateTime canceledAt
) {
	
	public static SellerSettlementLineResponse from(SellerSettlementLine line) {
		
		return new SellerSettlementLineResponse(
				line.getId(),
				line.getSellerSettlement() != null ? line.getSellerSettlement().getId() : null,
				line.getSellerId(),
				line.getRentalItemId(),
				line.getMemberId(),
				line.getProductId(),
				line.getRentalAmount(),
				line.getFeeAmount(),
				line.getStatus() != null ? line.getStatus().name() : null,
				line.getCanceledAt()
		);
	}
}
