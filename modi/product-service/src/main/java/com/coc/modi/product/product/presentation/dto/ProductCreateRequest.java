package com.coc.modi.product.product.presentation.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.coc.modi.product.product.domain.ProductCategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProductCreateRequest(
		
		@NotBlank(message = "상품명은 필수입니다.")
        String name,
		
		@NotBlank(message = "상품 설명은 필수입니다.")
        String description,
		
		@NotNull(message = "대여 금액은 필수입니다.")
		@Positive(message = "대여 금액은 0보다 커야 합니다.")
        BigDecimal pricePerDay,
		
		@NotNull(message = "카테고리는 필수입니다.")
		ProductCategory category,

		Map<String, String> specs,
		
        List<String> images
) {
}
