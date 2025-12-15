package com.coc.modi.account.deposit.presentation.dto;

import com.coc.modi.account.deposit.application.dto.DepositCommand;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DepositRequest(
		@NotNull(message = "amount는 필수입니다.")
		@Positive(message = "amount는 0보다 커야 합니다.")
		BigDecimal amount
) {

    public DepositCommand toCommand(Long memberId) {

        return new DepositCommand(
                memberId,
                amount
        );
    }
}
