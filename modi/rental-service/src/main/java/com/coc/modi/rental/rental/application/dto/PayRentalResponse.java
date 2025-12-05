package com.coc.modi.rental.rental.application.dto;

import com.coc.modi.rental.rental.domain.Rental;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PayRentalResponse(
        LocalDateTime paidAt,
        BigDecimal amount,
        BigDecimal balance
) {
    public static PayRentalResponse create(Rental rental, BigDecimal balance) {

        return new PayRentalResponse(rental.getPaidAt(), rental.getTotalAmount(), balance);
    }
}
