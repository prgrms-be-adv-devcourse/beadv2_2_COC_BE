package com.coc.modi.seller.settlement.batch;

import java.math.BigDecimal;

public record SettlementAggregationItem(
		Long sellerId,
		String periodYm,
		Long rentalItemId,
		Long memberId,
		Long productId,
		BigDecimal rentalAmount
) {
}
