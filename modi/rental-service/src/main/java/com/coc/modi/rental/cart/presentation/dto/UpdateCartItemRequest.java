package com.coc.modi.rental.cart.presentation.dto;

import com.coc.modi.rental.cart.application.dto.UpdateCartItemCommand;

import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

public record UpdateCartItemRequest(
		@NotNull @FutureOrPresent LocalDate startDate,
		@NotNull @FutureOrPresent LocalDate endDate
) {

    public UpdateCartItemCommand toCommand(Long memberId, Long cartItemId) {

        return new UpdateCartItemCommand(memberId, cartItemId, startDate, endDate);
    }
}
