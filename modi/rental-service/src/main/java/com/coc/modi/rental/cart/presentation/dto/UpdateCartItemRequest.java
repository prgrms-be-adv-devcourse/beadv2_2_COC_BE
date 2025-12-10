package com.coc.modi.rental.cart.presentation.dto;

import com.coc.modi.rental.cart.application.dto.UpdateCartItemCommand;

import java.time.LocalDate;

public record UpdateCartItemRequest(
        LocalDate startDate,
        LocalDate endDate
) {

    public UpdateCartItemCommand toCommand(Long memberId, Long cartItemId) {

        return new UpdateCartItemCommand(memberId, cartItemId, startDate, endDate);
    }
}
