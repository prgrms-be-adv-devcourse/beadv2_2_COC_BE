package com.coc.modi.seller.settlement.presentation.admin.dto;

import com.coc.modi.seller.settlement.domain.SellerSettlementStatus;

public record SettlementBulkPayRequest(
		String periodYm,
		Long sellerId,
		SellerSettlementStatus status,
		String paidAt
) {
}
