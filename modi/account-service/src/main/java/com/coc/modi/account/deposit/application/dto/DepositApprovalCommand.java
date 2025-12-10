package com.coc.modi.account.deposit.application.dto;

import java.math.BigDecimal;

public record DepositApprovalCommand(
        String paymentKey,
        String orderId,
        BigDecimal amount
) {
}
