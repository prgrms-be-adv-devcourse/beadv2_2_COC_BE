package com.coc.modi.rental.rental.infrastructure.client.dto;

import java.util.List;

public record UnavailableProductsResponse(
		List<Long> unavailableProductIds
) {
}
