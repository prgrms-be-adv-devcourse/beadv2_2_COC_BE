package com.coc.modi.rental.cart.infrastructure;

import com.coc.modi.rental.cart.domain.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartJpaRepository extends JpaRepository<Cart, Long> {

}
