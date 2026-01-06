package com.coc.modi.seller.settlement.infrastructure.client.wallet.dto;

import java.math.BigDecimal;

public record SettlementPayoutRequest(
        Long memberId,
        Long settlementId,
        BigDecimal amount
) {
}
