package com.coc.modi.rental.application.dto;

public record RentalReturnResponse(
        Long rentalId,
        String status,
        String extraFeeAmount
) {
}
