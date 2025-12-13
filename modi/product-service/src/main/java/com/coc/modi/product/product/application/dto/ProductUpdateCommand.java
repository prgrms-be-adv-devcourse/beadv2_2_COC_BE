package com.coc.modi.product.product.application.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record ProductUpdateCommand(
		
		@NotBlank(message = "name is required")
		String name,
		
		@NotBlank(message = "description is required")
		String description,
		
		@NotNull(message = "pricePerDay is required")
		@Positive(message = "pricePerDay must be positive")
		BigDecimal pricePerDay,
		
		@NotBlank(message = "category is required")
		String category,
		
		List<ProductResponse.ImageInfo> images
) {
}