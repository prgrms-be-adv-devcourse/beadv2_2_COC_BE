package com.coc.modi.rental.cart.infrastructure;

import com.coc.modi.rental.cart.domain.CartItem;
import com.coc.modi.rental.cart.domain.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CartItemRepositoryAdapter implements CartItemRepository {

    private final CartItemJpaRepository cartItemJpaRepository;

    @Override
    public List<CartItem> findAllByIdIn(List<Long> ids) {

        return cartItemJpaRepository.findAllByIdIn(ids);
    }
}
