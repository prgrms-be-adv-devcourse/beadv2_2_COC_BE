package com.coc.modi.rental.rental.presentation.dto;

import com.coc.modi.rental.rental.application.dto.RentalReturnCommand;

import java.math.BigDecimal;

public record RentalReturnRequest(
        BigDecimal damageFee,
        String damageReason,
        BigDecimal lateFee,
        String lateReason,
        String memo
) {
    public RentalReturnCommand toCommand(Long rentalItemId, Long memberId) {

        return new RentalReturnCommand(
                memberId,
                rentalItemId,
                damageFee,
                damageReason,
                lateFee,
                lateReason,
                memo
        );
    }
}
