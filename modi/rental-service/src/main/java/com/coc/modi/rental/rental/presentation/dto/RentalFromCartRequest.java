package com.coc.modi.rental.rental.presentation.dto;

import com.coc.modi.rental.rental.application.dto.CreateRentalFromCartCommand;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RentalFromCartRequest(
		@NotEmpty List<@NotNull @Positive Long> cartItemIds
) {
	
	public CreateRentalFromCartCommand toCommand(Long memberId) {
		
		return new CreateRentalFromCartCommand(memberId, cartItemIds);
	}
}
