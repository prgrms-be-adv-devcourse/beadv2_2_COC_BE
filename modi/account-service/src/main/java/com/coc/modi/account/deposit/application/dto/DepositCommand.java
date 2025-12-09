package com.coc.modi.account.deposit.application.dto;

import java.math.BigDecimal;

public record DepositCommand(
        Long memberId,
        BigDecimal amount
) {
}
