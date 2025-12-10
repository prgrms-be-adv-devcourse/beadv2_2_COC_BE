package com.coc.modi.rental.cart.presentation.dto;

import com.coc.modi.rental.cart.application.dto.RemoveCartItemsCommand;

import java.util.List;

public record DeleteCartItemsRequest(
        Long memberId,
        List<Long> cartItemIds
) {

    public RemoveCartItemsCommand toCommand() {

        return new RemoveCartItemsCommand(memberId, cartItemIds);
    }
}
