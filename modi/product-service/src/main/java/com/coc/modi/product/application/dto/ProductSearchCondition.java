package com.coc.modi.product.application.dto;

import com.coc.modi.product.domain.ProductCategory;

import java.math.BigDecimal;

public record ProductSearchCondition(
        String keyword,            // 키워드 (name / description)
        ProductCategory category,  // 카테고리
        BigDecimal minPrice,       // 최저 일일 가격
        BigDecimal maxPrice,       // 최고 일일 가격
        Long sellerId
) {
    
    public boolean isEmpty() {
        return (keyword == null || keyword.isBlank()) &&
                category == null &&
                minPrice == null &&
                maxPrice == null &&
                sellerId == null;
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }
}
