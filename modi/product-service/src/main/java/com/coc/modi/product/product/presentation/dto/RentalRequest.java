package com.coc.modi.product.product.presentation.dto;

import java.time.LocalDate;
import java.util.List;

public record RentalRequest(
		LocalDate startDate,
		LocalDate endDate,
		List<Long> productIds
) {
}
