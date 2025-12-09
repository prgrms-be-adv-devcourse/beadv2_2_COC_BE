package com.coc.modi.account.deposit.presentation.dto;

import com.coc.modi.account.deposit.application.dto.DepositApprovalCommand;

import java.math.BigDecimal;

public record DepositApprovalRequest (
        String paymentKey,
        String orderId,
        BigDecimal amount
) {
    public DepositApprovalCommand toCommand(){

        return new DepositApprovalCommand(
                paymentKey,
                orderId,
                amount
        );
    }
}
