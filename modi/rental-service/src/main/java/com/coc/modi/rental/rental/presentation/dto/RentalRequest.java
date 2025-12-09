package com.coc.modi.rental.rental.presentation.dto;

import com.coc.modi.rental.rental.application.dto.RentalCreateCommand;

import java.time.LocalDate;

public record RentalRequest(
		Long productId,
		LocalDate startDate,
		LocalDate endDate
) {
	public RentalCreateCommand toCommand(Long memberId) {
		
		return new RentalCreateCommand(memberId, productId, startDate, endDate);
	}
}
