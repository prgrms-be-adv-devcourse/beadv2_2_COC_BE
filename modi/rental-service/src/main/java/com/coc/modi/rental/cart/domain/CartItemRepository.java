package com.coc.modi.rental.cart.domain;

import java.util.List;

public interface CartItemRepository {

    List<CartItem> findAllByIdIn(List<Long> ids);
}
