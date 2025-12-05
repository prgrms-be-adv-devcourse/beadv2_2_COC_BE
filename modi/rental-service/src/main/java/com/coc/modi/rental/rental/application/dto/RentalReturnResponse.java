package com.coc.modi.rental.rental.application.dto;

public record RentalReturnResponse(
        Long rentalId,
        String status,
        String extraFeeAmount
) {
}
