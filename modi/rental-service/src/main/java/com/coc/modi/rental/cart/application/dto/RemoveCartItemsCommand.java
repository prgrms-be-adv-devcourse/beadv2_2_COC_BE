package com.coc.modi.rental.cart.application.dto;

import java.util.List;

public record RemoveCartItemsCommand(
        Long memberId,
        List<Long> cartItemIds
) {
}
