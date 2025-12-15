package com.coc.modi.rental.rental.presentation.dto;

import com.coc.modi.rental.rental.application.dto.ExtendRentalCommand;

import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

public record ExtendRentalRequest(
		@NotNull @FutureOrPresent LocalDate newEndDate
) {
	public ExtendRentalCommand toCommand(Long rentalItemId, Long memberId) {
		
		return new ExtendRentalCommand(rentalItemId, memberId, newEndDate);
	}
}
