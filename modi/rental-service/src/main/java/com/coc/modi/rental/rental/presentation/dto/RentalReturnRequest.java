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
    public RentalReturnCommand toCommand(Long rentalId, Long memberId) {

        return new RentalReturnCommand(
                memberId,
                rentalId,
                damageFee,
                damageReason,
                lateFee,
                lateReason,
                memo
        );
    }
}
