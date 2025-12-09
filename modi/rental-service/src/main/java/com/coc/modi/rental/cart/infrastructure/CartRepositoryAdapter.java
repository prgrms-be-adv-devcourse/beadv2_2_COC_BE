package com.coc.modi.rental.cart.infrastructure;

import com.coc.modi.rental.cart.domain.Cart;
import com.coc.modi.rental.cart.domain.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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
}
