package com.coc.modi.product.application.dto;

import java.util.List;

public record RentalResponse(
		List<Long> unavailableProductIds
) {
}
