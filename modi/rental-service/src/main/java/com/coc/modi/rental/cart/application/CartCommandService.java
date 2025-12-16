package com.coc.modi.rental.cart.application;

import com.coc.modi.rental.cart.application.dto.AddCartItemCommand;
import com.coc.modi.rental.cart.application.dto.RemoveCartItemsCommand;
import com.coc.modi.rental.cart.application.dto.UpdateCartItemCommand;
import com.coc.modi.rental.cart.domain.Cart;
import com.coc.modi.rental.cart.domain.CartItem;
import com.coc.modi.rental.cart.domain.CartRepository;
import com.coc.modi.common.ErrorCode;
import com.coc.modi.rental.rental.exception.RentalException;
import com.coc.modi.rental.rental.infrastructure.client.ProductFeignClient;
import com.coc.modi.rental.rental.infrastructure.client.dto.ProductResponseDto;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartCommandService {

    private static final String ACTIVE_STATUS = "ACTIVE";

    private final CartRepository cartRepository;
    private final ProductFeignClient productFeignClient;

    @Transactional
    public void addItem(AddCartItemCommand command) {

        validateDate(command.startDate(), command.endDate());
        ProductResponseDto product = fetchProduct(command.productId());

        validateProductActive(product);

        Long itemId = cartRepository.nextItemId(command.memberId());
        CartItem newItem = CartItem.create(itemId, command.productId(), command.startDate(), command.endDate());

        cartRepository.upsertItem(command.memberId(), newItem);
    }

    @Transactional
    public void updateItem(UpdateCartItemCommand command) {

        validateDate(command.startDate(), command.endDate());

        Cart cart = cartRepository.findByMemberId(command.memberId())
                .orElseThrow(() -> new IllegalArgumentException("장바구니가 존재하지 않습니다."));

        CartItem target = cart.getItems().stream()
                .filter(item -> item.getId().equals(command.cartItemId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("장바구니 항목을 찾을 수 없습니다. cartItemId: " + command.cartItemId()));

        target.updatePeriod(command.startDate(), command.endDate());
        cart.touch();
        cartRepository.save(cart);
    }

    @Transactional
    public void deleteItem(Long memberId, Long cartItemId) {

        cartRepository.removeItems(memberId, List.of(cartItemId));
    }
	

    private void validateDate(LocalDate startDate, LocalDate endDate) {

        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {

            throw new IllegalArgumentException("대여 기간이 올바르지 않습니다.");
        }
    }

    private void validateProductActive(ProductResponseDto product) {

        if (product == null || !ACTIVE_STATUS.equals(product.status())) {

            throw new IllegalArgumentException("현재 판매중인 상품이 아닙니다. productId: "
                    + (product == null ? null : product.productId()));
        }
    }

    private ProductResponseDto fetchProduct(Long productId) {

        try {
            return productFeignClient.getProducts(productId);
        } catch (FeignException ex) {
            throw new RentalException(ErrorCode.PRODUCT_INTERNAL_ERROR, "상품 서비스 호출에 실패했습니다.");
        }
    }
}
