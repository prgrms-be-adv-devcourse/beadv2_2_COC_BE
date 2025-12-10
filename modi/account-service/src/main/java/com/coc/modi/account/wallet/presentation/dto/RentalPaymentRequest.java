package com.coc.modi.account.wallet.presentation.dto;

import java.math.BigDecimal;

public record RentalPaymentRequest(
        Long memberId,
        Long rentalId,
        BigDecimal amount
) {
}
