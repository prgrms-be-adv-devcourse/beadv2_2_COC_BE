package com.coc.modi.rental.cart.domain;

import java.util.List;
import java.util.Optional;

public interface CartRepository {

    Optional<Cart> findByMemberId(Long memberId);

    void save(Cart cart);

    void deleteByMemberId(Long memberId);

    Cart upsertItem(Long memberId, CartItem newItem);

    Cart removeItems(Long memberId, List<Long> itemIds);

    Long nextItemId(Long memberId);
}
