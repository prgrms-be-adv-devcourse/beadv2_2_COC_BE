package com.coc.modi.rental.cart.infrastructure;

import com.coc.modi.rental.cart.domain.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartItemJpaRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findAllByIdIn(List<Long> ids);
}
