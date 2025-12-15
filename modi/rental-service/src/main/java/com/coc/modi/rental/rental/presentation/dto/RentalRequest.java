package com.coc.modi.rental.rental.presentation.dto;

import com.coc.modi.rental.rental.application.dto.RentalCreateCommand;

import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RentalRequest(
		@NotNull @Positive Long productId,
		@NotNull @FutureOrPresent LocalDate startDate,
		@NotNull @FutureOrPresent LocalDate endDate
) {
	public RentalCreateCommand toCommand(Long memberId) {
		
		return new RentalCreateCommand(memberId, productId, startDate, endDate);
	}
}
