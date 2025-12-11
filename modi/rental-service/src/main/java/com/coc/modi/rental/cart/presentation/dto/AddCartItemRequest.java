package com.coc.modi.rental.cart.presentation.dto;

import com.coc.modi.rental.cart.application.dto.AddCartItemCommand;

import java.time.LocalDate;

public record AddCartItemRequest(
        Long productId,
        LocalDate startDate,
        LocalDate endDate
) {

    public AddCartItemCommand toCommand(Long memberId) {

        return new AddCartItemCommand(memberId, productId, startDate, endDate);
    }
}
