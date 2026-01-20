package com.coc.modi.rental.rental.infrastructure.client.dto;

public record RentalItemSellerResponse(
		Long rentalItemId,
		Long sellerId,
		Long memberId
) {
}
