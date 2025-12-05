package com.coc.modi.rental.rental.infrastructure.client.dto;

import java.math.BigDecimal;

public record WalletInfoResponse(
        Long walletId,
        Long memberId,
        BigDecimal balance
) {
}
