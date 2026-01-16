package com.coc.modi.delivery.delivery.infrastructure.client.rental.dto;

public record RentalItemSellerResponse(
		Long rentalItemId,
		Long sellerId,
		Long memberId
) {
}
