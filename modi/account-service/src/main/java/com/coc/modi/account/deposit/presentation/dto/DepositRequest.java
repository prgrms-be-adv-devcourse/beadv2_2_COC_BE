package com.coc.modi.account.deposit.presentation.dto;

import com.coc.modi.account.deposit.application.dto.DepositCommand;

import java.math.BigDecimal;

public record DepositRequest(
        BigDecimal amount
) {

    public DepositCommand toCommand(Long memberId) {

        return new DepositCommand(
                memberId,
                amount
        );
    }
}
