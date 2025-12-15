package com.coc.modi.rental.rental.infrastructure.client.dto;

import java.time.LocalDate;

import com.coc.modi.rental.rental.domain.RentalItemStatus;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RentalInternalSearchCondition(
		@NotNull @Positive Long sellerId,
		@Positive Long productId,
		@NotNull RentalItemStatus status,
		@NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
		@NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
) {
}
