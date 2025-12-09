package com.coc.modi.rental.cart.infrastructure;

import com.coc.modi.rental.cart.domain.CartItem;
import com.coc.modi.rental.cart.domain.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CartItemRepositoryAdapter implements CartItemRepository {

    private final CartRedisRepository cartRedisRepository;

    @Override
    public List<CartItem> findAllByIdIn(List<Long> ids) {

        // CartItem 단건 조회는 Cart를 불러와서 필터링하는 상위 서비스에서 처리하도록 위임.
        throw new UnsupportedOperationException("Use CartRepository to fetch cart then filter items.");
    }

    @Override
	public Optional<CartItem> findById(Long id) {

        throw new UnsupportedOperationException("Use CartRepository to fetch cart then filter items.");
    }
}
