package com.coc.modi.rental.rental.presentation.dto;

import com.coc.modi.rental.rental.application.dto.ExtendRentalCommand;

import java.time.LocalDate;

public record ExtendRentalRequest(
		LocalDate newEndDate
) {
	public ExtendRentalCommand toCommand(Long rentalItemId, Long memberId) {
		
		return new ExtendRentalCommand(rentalItemId, memberId, newEndDate);
	}
}
