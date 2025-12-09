package com.coc.modi.rental.rental.infrastructure.client.dto;

import java.time.LocalDate;

import com.coc.modi.rental.rental.domain.RentalItemStatus;
import org.springframework.format.annotation.DateTimeFormat;

public record RentalInternalSearchCondition(
		Long sellerId,
		Long productId,
		RentalItemStatus status,
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
) {
}
