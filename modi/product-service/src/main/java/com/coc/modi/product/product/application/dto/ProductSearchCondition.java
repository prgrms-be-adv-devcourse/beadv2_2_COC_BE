package com.coc.modi.product.product.application.dto;

import com.coc.modi.product.product.domain.ProductCategory;
import com.coc.modi.product.search.ProductSortType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProductSearchCondition(
        String keyword,            // 키워드 (name / description)
        ProductCategory category,  // 카테고리
        BigDecimal minPrice,       // 최저 일일 가격
        BigDecimal maxPrice,       // 최고 일일 가격
        Long sellerId,
		LocalDate startDate,
		LocalDate endDate,
		ProductSortType sortType
) {
	
	public boolean hasRentalPeriod() {
		return startDate != null && endDate != null;
	}
	
	public ProductSortType effectiveSortType() {
		return sortType != null ? sortType : ProductSortType.LATEST;
	}
}
