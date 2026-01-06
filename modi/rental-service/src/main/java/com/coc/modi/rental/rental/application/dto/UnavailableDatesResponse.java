package com.coc.modi.rental.rental.application.dto;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

public record UnavailableDatesResponse(
		
		Long productId,
		
		@JsonFormat(pattern = "yyyy-MM")
		YearMonth ym,
		
		@JsonFormat(pattern = "yyyy-MM-dd")
		List<LocalDate> unavailableDates
) {
}
