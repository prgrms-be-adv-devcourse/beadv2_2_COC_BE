package com.coc.modi.rental.application.dto;

import java.math.BigDecimal;

public record RentalReturnCommand(
        Long memberId,
        Long rentalId,
        BigDecimal damageFee,
        String damageReason,
        BigDecimal lateFee,
        String lateReason,
        String memo
) {
}
