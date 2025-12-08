package com.coc.modi.rental.rental.infrastructure.client.dto;

import java.math.BigDecimal;

public record ChargeWalletCommand(
        Long memberId,
        Long rentalId,
        BigDecimal amount
) {
}
