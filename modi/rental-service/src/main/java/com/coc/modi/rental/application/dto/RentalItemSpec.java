package com.coc.modi.rental.application.dto;

import java.time.LocalDate;

public record RentalItemSpec(

        Long productId,
        LocalDate startDate,
        LocalDate endDate
) {
}
