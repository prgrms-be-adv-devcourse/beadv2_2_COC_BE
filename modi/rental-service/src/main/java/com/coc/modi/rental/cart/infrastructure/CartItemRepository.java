package com.coc.modi.rental.cart.infrastructure;

import com.coc.modi.rental.cart.domain.CartItem;

import java.util.List;

public interface CartItemRepository {

    List<CartItem> findAllByIdIn(List<Long> ids);
}
