package com.coc.modi.account.deposit.application.dto;

import java.math.BigDecimal;

public record DepositCancelCommand(
        Long memberId,
        String paymentKey,
        String orderId,
        BigDecimal cancelAmount,
        String cancelReason
) {
}
