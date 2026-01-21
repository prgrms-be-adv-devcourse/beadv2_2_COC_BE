package com.coc.modi.account.deposit.application.dto;

import com.coc.modi.account.deposit.domain.PgDeposit;
import com.coc.modi.account.deposit.domain.PgDepositStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DepositResponse(
        Long id,
        Long memberId,
        BigDecimal amount,
        BigDecimal feeAmount,
        BigDecimal totalAmount,
        PgDepositStatus status,
        String pgProvider,
        String orderId,
        LocalDateTime requestedAt,
        LocalDateTime approvedAt,
        String failedReason,
        String paymentKey
) {

    public static DepositResponse from(PgDeposit pgDeposit) {

        return new DepositResponse(
                pgDeposit.getId(),
                pgDeposit.getMemberId(),
                pgDeposit.getAmount(),
                pgDeposit.getFeeAmount(),
                pgDeposit.getTotalAmount(),
                pgDeposit.getStatus(),
                pgDeposit.getPgProvider(),
                pgDeposit.getPgTid(),
                pgDeposit.getRequestedAt(),
                pgDeposit.getApprovedAt(),
                pgDeposit.getFailedReason(),
                pgDeposit.getPaymentKey()
        );
    }
}
