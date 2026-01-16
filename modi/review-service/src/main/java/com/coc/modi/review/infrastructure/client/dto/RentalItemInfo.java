package com.coc.modi.review.infrastructure.client.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RentalItemInfo(
		Long rentalItemId,
		Long productId,
		Long memberId,
		Long sellerId,
		String status,
		LocalDateTime returnedAt
) {
}
