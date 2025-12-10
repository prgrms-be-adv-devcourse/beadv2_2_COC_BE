package com.coc.modi.rental.rental.application.dto;

import com.coc.modi.rental.rental.domain.Rental;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PayRentalResponse(
		Long rentalId,
		LocalDateTime paidAt,
		BigDecimal amount,
		BigDecimal balance,
		String rentalStatus
) {
	public static PayRentalResponse create(Rental rental,
										   BigDecimal amount,
										   BigDecimal balance,
										   LocalDateTime paidAt) {
		
		return new PayRentalResponse(
				rental.getId(),
				paidAt,
				amount,
				balance,
				rental.getStatus().name()
		);
	}
}
