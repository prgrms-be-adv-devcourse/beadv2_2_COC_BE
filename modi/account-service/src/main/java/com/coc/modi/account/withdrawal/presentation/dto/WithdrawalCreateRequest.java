package com.coc.modi.account.withdrawal.presentation.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record WithdrawalCreateRequest(
        @NotNull(message = "amount는 필수입니다.")
        @Positive(message = "amount는 0보다 커야 합니다.")
        BigDecimal amount
) {
}
