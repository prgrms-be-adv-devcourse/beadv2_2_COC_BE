package com.coc.modi.account.wallet.presentation.dto;

import java.math.BigDecimal;

public record RentalRefundRequest(
        Long memberId,
        Long rentalId,
        Long rentalItemId,
        BigDecimal amount
) {
}
