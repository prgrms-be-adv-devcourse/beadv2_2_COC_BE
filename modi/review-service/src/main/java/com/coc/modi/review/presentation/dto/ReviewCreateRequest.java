package com.coc.modi.review.presentation.dto;

import com.coc.modi.review.application.dto.CreateReviewCommand;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReviewCreateRequest(
		@NotNull Long rentalItemId,
		@NotNull Long sellerId,
		@NotNull @Min(1) @Max(5) Short rating,
		@NotBlank String content
) {

	public CreateReviewCommand toCommand(Long memberId) {
		
		return new CreateReviewCommand(rentalItemId, sellerId, memberId, rating, content);

	}
}
