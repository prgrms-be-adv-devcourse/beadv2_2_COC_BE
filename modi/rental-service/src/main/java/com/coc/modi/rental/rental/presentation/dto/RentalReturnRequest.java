package com.coc.modi.rental.rental.presentation.dto;

import com.coc.modi.rental.rental.application.dto.RentalReturnCommand;

import java.math.BigDecimal;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record RentalReturnRequest(
		@PositiveOrZero BigDecimal damageFee,
		@Size(max = 255) String damageReason,
		@PositiveOrZero BigDecimal lateFee,
		@Size(max = 255) String lateReason,
		@Size(max = 500) String memo
) {
	public RentalReturnCommand toCommand(Long rentalItemId, Long memberId) {
		
		return new RentalReturnCommand(
				memberId,
				rentalItemId,
				damageFee,
				damageReason,
				lateFee,
				lateReason,
				memo
		);
	}
}
