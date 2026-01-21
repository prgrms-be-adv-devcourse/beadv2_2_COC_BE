package com.coc.modi.product.product.presentation.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record ProductBulkRequest(
		@NotEmpty(message = "상품 ID 목록은 필수입니다.")
		List<@NotNull(message = "상품 ID는 필수입니다.") Long> productIds
) {
}
