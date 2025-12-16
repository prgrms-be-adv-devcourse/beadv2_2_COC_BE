package com.coc.modi.rental.cart.presentation.dto;

import com.coc.modi.rental.cart.application.dto.RemoveCartItemsCommand;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DeleteCartItemsRequest(
		@NotNull @Positive Long memberId,
		@NotEmpty List<@NotNull @Positive Long> cartItemIds
) {

    public RemoveCartItemsCommand toCommand() {

        return new RemoveCartItemsCommand(memberId, cartItemIds);
    }
}
