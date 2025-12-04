package com.coc.modi.cart.infrastructure;

import com.coc.modi.cart.domain.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartJpaRepository extends JpaRepository<Cart, Long> {

}
