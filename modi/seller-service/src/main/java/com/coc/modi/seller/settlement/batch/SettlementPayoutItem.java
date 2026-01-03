package com.coc.modi.seller.settlement.batch;

import java.math.BigDecimal;

public record SettlementPayoutItem(
		Long settlementId,
		Long sellerId,
		Long memberId,
		BigDecimal amount
) {
}
