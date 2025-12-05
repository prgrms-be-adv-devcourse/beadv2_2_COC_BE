package com.coc.modi.rental.rental.application.dto;

import java.time.LocalDate;

public record RentalCreateCommand(
        Long memberId,
        Long productId,
        LocalDate startDate,
        LocalDate endDate
) {


}
