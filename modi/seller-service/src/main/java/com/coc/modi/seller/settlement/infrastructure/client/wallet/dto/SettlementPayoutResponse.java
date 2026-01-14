package com.coc.modi.seller.settlement.infrastructure.client.wallet.dto;

import java.math.BigDecimal;

public record SettlementPayoutResponse(
        Long walletId,
        Long memberId,
        BigDecimal balance
) {
}
