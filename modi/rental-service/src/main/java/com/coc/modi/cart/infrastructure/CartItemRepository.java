package com.coc.modi.cart.infrastructure;

import com.coc.modi.cart.domain.CartItem;

import java.util.List;

public interface CartItemRepository {

    List<CartItem> findAllByIdIn(List<Long> ids);
}
