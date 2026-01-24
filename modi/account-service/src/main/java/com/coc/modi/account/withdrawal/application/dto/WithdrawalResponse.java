package com.coc.modi.account.withdrawal.application.dto;

import com.coc.modi.account.withdrawal.domain.WithdrawalRequest;
import com.coc.modi.account.withdrawal.domain.WithdrawalStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WithdrawalResponse(
        Long id,
        WithdrawalStatus status,
        BigDecimal requestedAmount,
        BigDecimal feeAmount,
        BigDecimal payoutAmount,
        LocalDateTime requestedAt,
        LocalDateTime processedAt
) {

    public static WithdrawalResponse from(WithdrawalRequest request) {

        return new WithdrawalResponse(
                request.getId(),
                request.getStatus(),
                request.getRequestedAmount(),
                request.getFeeAmount(),
                request.getPayoutAmount(),
                request.getCreatedAt(),
                request.getProcessedAt()
        );
    }
}
