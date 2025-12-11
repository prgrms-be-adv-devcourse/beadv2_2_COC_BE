package com.coc.modi.rental.rental.application.dto;

import java.time.LocalDate;

public record ExtendRentalCommand(
		Long rentalItemId,
		Long memberId,
		LocalDate newEndDate
) {
}
