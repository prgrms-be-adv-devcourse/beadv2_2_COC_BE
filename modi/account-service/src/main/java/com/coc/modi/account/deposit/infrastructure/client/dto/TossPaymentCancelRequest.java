package com.coc.modi.account.deposit.infrastructure.client.dto;

import java.math.BigDecimal;

public record TossPaymentCancelRequest(
        String cancelReason,
        BigDecimal cancelAmount
) {
}
