package com.coc.modi.account.deposit.infrastructure.client.dto;

import java.math.BigDecimal;

// Toss에서 요청하는 결제 승인 파라미터
public record TossPaymentApprovalRequest(
        String paymentKey,
        String orderId,
        BigDecimal amount
) {
}
