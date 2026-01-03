package com.coc.modi.rental.cart.application;

import com.coc.modi.rental.cart.application.dto.CartItemResponse;
import com.coc.modi.rental.cart.application.dto.CartResponse;
import com.coc.modi.rental.cart.domain.Cart;
import com.coc.modi.rental.cart.domain.CartItem;
import com.coc.modi.rental.cart.domain.CartRepository;
import com.coc.modi.rental.rental.infrastructure.client.ProductClientAdapter;
import com.coc.modi.rental.rental.infrastructure.client.dto.ProductResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartQueryService {

    private final CartRepository cartRepository;
    private final ProductClientAdapter productClientAdapter;

    @Transactional(readOnly = true)
    public CartResponse getCart(Long memberId) {

        Cart cart = cartRepository.findByMemberId(memberId).orElse(null);

        if (cart == null || cart.getItems().isEmpty()) {

            return new CartResponse(Collections.emptyList(), null);
        }

        List<Long> productIds = cart.getItems().stream()
                .map(CartItem::getProductId)
                .distinct()
                .toList();

        List<ProductResponseDto> products = productClientAdapter.getProducts(productIds);

        Map<Long, ProductResponseDto> productMap = products.stream()
                .collect(Collectors.toMap(ProductResponseDto::productId, dto -> dto));

        List<CartItemResponse> items = cart.getItems().stream()
                .map(item -> toItemResponse(item, productMap))
                .toList();

        return new CartResponse(items, cart.getUpdatedAt());
    }

    private CartItemResponse toItemResponse(com.coc.modi.rental.cart.domain.CartItem item,
                                            Map<Long, ProductResponseDto> productMap) {

        ProductResponseDto product = productMap.get(item.getProductId());

        if (product == null) {

            throw new IllegalArgumentException("상품 정보를 찾을 수 없습니다. productId: " + item.getProductId());
        }

        return new CartItemResponse(
                item.getId(),
                item.getProductId(),
                item.getStartDate(),
                item.getEndDate(),
                product.price(),
                product.status()
        );
    }
}
