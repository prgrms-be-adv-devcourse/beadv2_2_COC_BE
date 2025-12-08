package com.coc.modi.rental.rental.application.dto;

import java.math.BigDecimal;

public record RentalReturnCommand(
        Long memberId,
        Long rentalItemId,
        BigDecimal damageFee,
        String damageReason,
        BigDecimal lateFee,
        String lateReason,
        String memo
) {
}
