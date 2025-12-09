package com.coc.modi.rental.cart.infrastructure;

import com.coc.modi.rental.cart.domain.Cart;
import com.coc.modi.rental.cart.domain.CartItem;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CartRedisRepository {

    private static final String CART_KEY_PREFIX = "cart:";
    private static final Duration TTL = Duration.ofHours(24);

    private final RedisTemplate<String, Object> redisTemplate;

    public Optional<Cart> findByMemberId(Long memberId) {

        String key = key(memberId);
        Object value = redisTemplate.opsForValue().get(key);

        if (value instanceof Cart cart) {

            refreshTtl(key);
            return Optional.of(cart);
        }

        return Optional.empty();
    }

    public void save(Cart cart) {

        String key = key(cart.getMemberId());
        redisTemplate.opsForValue().set(key, cart, TTL);
    }

    public void deleteByMemberId(Long memberId) {

        redisTemplate.delete(key(memberId));
    }

    public List<CartItem> findAllByIdIn(List<Long> ids) {

        // NOTE: 단건 조회가 필요하면 별도 색인 구조를 둘 수 있지만, 여기서는 전체 카트에서 필터.
        throw new UnsupportedOperationException("Use findByMemberId and filter items by caller's context.");
    }

    public Optional<CartItem> findById(Long id) {

        // NOTE: 단건 조회가 필요하면 별도 색인 구조를 둘 수 있지만, 여기서는 전체 카트에서 필터.
        throw new UnsupportedOperationException("Use findByMemberId and filter items by caller's context.");
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

    private String key(Long memberId) {

        return CART_KEY_PREFIX + memberId;
    }

    private void refreshTtl(String key) {

        redisTemplate.expire(key, TTL);
    }
}
