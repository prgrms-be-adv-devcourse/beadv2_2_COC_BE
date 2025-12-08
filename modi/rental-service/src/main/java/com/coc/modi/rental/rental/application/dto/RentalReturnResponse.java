package com.coc.modi.rental.rental.application.dto;

public record RentalReturnResponse(
        Long rentalId,
        Long rentalItemId,
        String status,
        String extraFeeAmount
) {
}
