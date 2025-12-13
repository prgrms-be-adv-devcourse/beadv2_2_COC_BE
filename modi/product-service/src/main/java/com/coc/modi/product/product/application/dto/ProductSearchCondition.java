package com.coc.modi.product.product.application.dto;

import com.coc.modi.product.product.domain.ProductCategory;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProductSearchCondition(
		String keyword,
		ProductCategory category,
		BigDecimal minPrice,
		BigDecimal maxPrice,
		Long sellerId,
		LocalDate startDate,
		LocalDate endDate
) {
	
	public boolean hasRentalPeriod() {
		
		return startDate != null && endDate != null;
	}
}
