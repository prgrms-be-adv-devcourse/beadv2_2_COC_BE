package com.coc.modi.account.deposit.presentation.dto;

import com.coc.modi.account.deposit.application.dto.DepositCancelCommand;

import java.math.BigDecimal;

public record DepositCancelRequest(
        String paymentKey,
        String orderId,
        BigDecimal amount,
        String reason
) {

    public DepositCancelCommand toCommand(Long memberId) {

        return new DepositCancelCommand(
                memberId,
                paymentKey,
                orderId,
                amount,
                reason
        );
    }
}
