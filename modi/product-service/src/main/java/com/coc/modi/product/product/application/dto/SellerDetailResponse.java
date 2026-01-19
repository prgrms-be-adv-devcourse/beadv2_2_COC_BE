package com.coc.modi.product.product.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SellerDetailResponse(
		Long sellerId,
		Long memberId
) {
}
