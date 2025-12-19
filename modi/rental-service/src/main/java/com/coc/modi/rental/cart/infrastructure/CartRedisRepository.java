package com.coc.modi.rental.cart.infrastructure;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.coc.modi.rental.cart.domain.Cart;
import com.coc.modi.rental.cart.domain.CartItem;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CartRedisRepository {

    private static final String CART_KEY_PREFIX = "cart:";
    private static final String CART_SEQ_KEY_PREFIX = "cart:seq:";
    private static final Duration TTL = Duration.ofHours(24);
    
    private final RedisTemplate<String, Cart> cartRedisTemplate;
    
    public Optional<Cart> findByMemberId(Long memberId) {

        String key = key(memberId);
        Cart cart = cartRedisTemplate.opsForValue().get(key);
        
        if (cart == null) {
            return Optional.empty();
        }
        
        refreshTtl(key);
        return Optional.of(cart);
    }

    public void save(Cart cart) {

        String key = key(cart.getMemberId());
        cartRedisTemplate.opsForValue().set(key, cart, TTL);
    }

    public void deleteByMemberId(Long memberId) {

        cartRedisTemplate.delete(key(memberId));
    }

    public Cart upsertItem(Long memberId, CartItem newItem) {

        Cart cart = findByMemberId(memberId).orElseGet(() -> Cart.create(memberId));

        List<CartItem> updated = cart.getItems().stream()
                .filter(item -> !item.getId().equals(newItem.getId()))
                .collect(Collectors.toCollection(java.util.ArrayList::new));

        updated.add(newItem);
        cart.replaceItems(updated);
        save(cart);

        return cart;
    }

    public Cart removeItems(Long memberId, List<Long> itemIds) {

        Cart cart = findByMemberId(memberId).orElseGet(() -> Cart.create(memberId));

        List<CartItem> updated = cart.getItems().stream()
                .filter(item -> !itemIds.contains(item.getId()))
                .collect(Collectors.toCollection(java.util.ArrayList::new));

        cart.replaceItems(updated);
        save(cart);

        return cart;
    }

    public Long nextItemId(Long memberId) {

        String seqKey = seqKey(memberId);
        Long next = cartRedisTemplate.opsForValue().increment(seqKey);

        cartRedisTemplate.expire(seqKey, TTL);
        return next;
    }

    private String key(Long memberId) {

        return CART_KEY_PREFIX + memberId;
    }

    private String seqKey(Long memberId) {

        return CART_SEQ_KEY_PREFIX + memberId;
    }

    private void refreshTtl(String key) {

        cartRedisTemplate.expire(key, TTL);
    }
}
