package com.coc.modi.rental.cart.domain;

import java.util.Optional;

public interface CartRepository {

    Optional<Cart> findByMemberId(Long memberId);

    void save(Cart cart);

    void deleteByMemberId(Long memberId);
}
