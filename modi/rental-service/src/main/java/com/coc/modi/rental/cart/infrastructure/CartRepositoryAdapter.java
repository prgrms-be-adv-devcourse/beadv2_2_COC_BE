package com.coc.modi.rental.cart.infrastructure;

import com.coc.modi.rental.cart.domain.Cart;
import com.coc.modi.rental.cart.domain.CartItem;
import com.coc.modi.rental.cart.domain.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CartRepositoryAdapter implements CartRepository {

    private final CartRedisRepository cartRedisRepository;

    @Override
    public Optional<Cart> findByMemberId(Long memberId) {

        return cartRedisRepository.findByMemberId(memberId);
    }

    @Override
    public void save(Cart cart) {

        cart.touch();
        cartRedisRepository.save(cart);
    }

    @Override
    public void deleteByMemberId(Long memberId) {

        cartRedisRepository.deleteByMemberId(memberId);
    }

    @Override
    public Cart upsertItem(Long memberId, CartItem newItem) {

        return cartRedisRepository.upsertItem(memberId, newItem);
    }

    @Override
    public Cart removeItems(Long memberId, List<Long> itemIds) {

        return cartRedisRepository.removeItems(memberId, itemIds);
    }

    @Override
    public Long nextItemId(Long memberId) {

        return cartRedisRepository.nextItemId(memberId);
    }
}
