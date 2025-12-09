package com.coc.modi.rental.cart.domain;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository {

    List<CartItem> findAllByIdIn(List<Long> ids);
	
	Optional<CartItem> findById(Long id);
}
