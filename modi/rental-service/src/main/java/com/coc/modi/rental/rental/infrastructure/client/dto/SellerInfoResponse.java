package com.coc.modi.rental.rental.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SellerInfoResponse(
		Long sellerId,
		Long memberId
) {
}
