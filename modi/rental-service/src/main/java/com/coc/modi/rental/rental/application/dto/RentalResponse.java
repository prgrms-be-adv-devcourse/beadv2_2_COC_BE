package com.coc.modi.rental.rental.application.dto;

import com.coc.modi.rental.rental.domain.Rental;

import java.time.LocalDateTime;
import java.util.List;

public record RentalResponse(
		Long rentalId,
		LocalDateTime paidAt,
		LocalDateTime createdAt,
		List<RentalItemResponse> items
) {
	
	public static RentalResponse create(Rental rental, List<RentalItemResponse> rentalItemResponseList) {
		
		return new RentalResponse(rental.getId(), rental.getPaidAt(), rental.getCreatedAt(), rentalItemResponseList);
	}
}
