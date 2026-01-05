package com.coc.modi.product.recommendation.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatTestRequest(
		@NotBlank(message = "메시지는 필수입니다.")
		String message
) {
}
