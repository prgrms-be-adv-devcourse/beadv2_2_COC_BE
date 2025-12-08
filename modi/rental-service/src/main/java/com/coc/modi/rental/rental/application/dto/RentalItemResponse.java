package com.coc.modi.rental.rental.application.dto;

import com.coc.modi.rental.rental.domain.RentalItem;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RentalItemResponse(
        Long rentalItemId,
        Long productId,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        BigDecimal unitPrice
) {

    public static RentalItemResponse from(RentalItem rentalItem) {

        return new RentalItemResponse(
                rentalItem.getId(),
                rentalItem.getProductId(),
                rentalItem.getStartDate(),
                rentalItem.getEndDate(),
                rentalItem.getStatus().toString(),
                rentalItem.getUnitPrice());
    }
}
