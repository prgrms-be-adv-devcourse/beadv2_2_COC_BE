package com.coc.modi.rental.cart.presentation.dto;

import com.coc.modi.rental.cart.application.dto.AddCartItemCommand;

import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddCartItemRequest(
		@NotNull @Positive Long productId,
		@NotNull @FutureOrPresent LocalDate startDate,
		@NotNull @FutureOrPresent LocalDate endDate
) {

    public AddCartItemCommand toCommand(Long memberId) {

        return new AddCartItemCommand(memberId, productId, startDate, endDate);
    }
}
