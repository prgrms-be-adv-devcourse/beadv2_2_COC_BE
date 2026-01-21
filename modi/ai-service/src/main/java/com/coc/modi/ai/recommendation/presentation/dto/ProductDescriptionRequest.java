package com.coc.modi.ai.recommendation.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Map;

public record ProductDescriptionRequest(
        @NotBlank(message = "상품명은 필수입니다.")
        String productName,
        @NotEmpty(message = "스펙은 필수입니다.")
        Map<String, String> specs,
        @NotBlank(message = "카테고리는 필수입니다.")
        String category
) {
}
