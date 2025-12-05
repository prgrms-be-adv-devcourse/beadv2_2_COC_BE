package com.coc.modi.rental.rental.application;

import com.coc.modi.rental.cart.domain.CartItem;
import com.coc.modi.rental.cart.infrastructure.CartItemRepository;
import com.coc.modi.common.ApiResponse;
import com.coc.modi.rental.rental.application.dto.CreateRentalFromCartCommand;
import com.coc.modi.rental.rental.application.dto.RentalCreateCommand;
import com.coc.modi.rental.rental.domain.Rental;
import com.coc.modi.rental.rental.domain.RentalItem;
import com.coc.modi.rental.rental.domain.RentalEventType;
import com.coc.modi.rental.rental.infrastructure.RentalRepository;
import com.coc.modi.rental.rental.infrastructure.client.ProductFeignClient;
import com.coc.modi.rental.rental.infrastructure.client.dto.ProductResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RentalCreationService {

    private final RentalRepository rentalRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductFeignClient productFeignClient;
    private final RentalEventLogService rentalEventLogService;

    @Transactional
    public ResponseEntity<ApiResponse<Void>> createRentalFromCart(CreateRentalFromCartCommand command) {

        List<CartItem> cartItems = cartItemRepository.findAllByIdIn(command.cartItemIds());

        if (cartItems.isEmpty()) {

            throw new IllegalArgumentException("장바구니 항목이 존재하지 않습니다.");
        }

        boolean hasDifferentMember = cartItems.stream()
                .anyMatch(cartItem -> !cartItem.getCart().getMemberId().equals(command.memberId()));

        if (hasDifferentMember) {

            throw new IllegalArgumentException("다른 회원의 장바구니 항목이 포함되어 있습니다.");
        }

        List<Long> productIds = cartItems.stream()
                .map(CartItem::getProductId)
                .distinct()
                .toList();

        List<ProductResponseDto> products = productFeignClient.getProducts(productIds);

        Map<Long, ProductResponseDto> productMap = products.stream()
                .collect(Collectors.toMap(ProductResponseDto::productId, dto -> dto));

        BigDecimal rentalTotalAmount = BigDecimal.ZERO;
        Rental rental = Rental.create(command.memberId(), rentalTotalAmount);

        for (CartItem cartItem : cartItems) {

            ProductResponseDto productResponseDto = productMap.get(cartItem.getProductId());

            if (productResponseDto == null) {

                throw new IllegalArgumentException("상품 가격 정보를 찾을 수 없습니다. productId: " + cartItem.getProductId());
            }

            if (!"ACTIVE".equals(productResponseDto.status())) {

                throw new IllegalArgumentException("현재 판매중인 상품이 아닙니다. productId: " + cartItem.getProductId());
            }

            BigDecimal unitPrice = productResponseDto.price();

            long rentalDays = ChronoUnit.DAYS.between(cartItem.getStartDate(), cartItem.getEndDate()) + 1;

            if (rentalDays <= 0) {

                throw new IllegalArgumentException("대여 종료일이 시작일보다 빠릅니다. cartItemId: " + cartItem.getId());
            }

            RentalItem rentalItem = RentalItem.create(
                    cartItem.getProductId(),
                    productResponseDto.sellerId(),
                    cartItem.getStartDate(),
                    cartItem.getEndDate(),
                    unitPrice
            );

            rental.addItem(rentalItem);

            rentalTotalAmount = rentalTotalAmount.add(unitPrice.multiply(BigDecimal.valueOf(rentalDays))
                    .setScale(2, RoundingMode.HALF_UP));
        }

        rental.updateTotalAmount(rentalTotalAmount);

        rentalRepository.save(rental);
        logCreatedEvent(rental);

        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @Transactional
    public ResponseEntity<ApiResponse<Void>> createRental(RentalCreateCommand command) {

        ProductResponseDto productResponseDto = productFeignClient.getProducts(command.productId());

        if (productResponseDto == null) {

            throw new IllegalArgumentException("상품 가격 정보를 찾을 수 없습니다. productId: " + command.productId());
        }

        if (!"ACTIVE".equals(productResponseDto.status())) {

            throw new IllegalArgumentException("현재 판매중인 상품이 아닙니다. productId: " + command.productId());
        }

        BigDecimal rentalTotalAmount = BigDecimal.ZERO;
        Rental rental = Rental.create(command.memberId(), rentalTotalAmount);

        BigDecimal unitPrice = productResponseDto.price();

        long rentalDays = ChronoUnit.DAYS.between(command.startDate(), command.endDate()) + 1;

        if (rentalDays <= 0) {

            throw new IllegalArgumentException("대여 종료일이 시작일보다 빠릅니다. productId: " + command.productId());
        }

        RentalItem rentalItem = RentalItem.create(
                command.productId(),
                productResponseDto.sellerId(),
                command.startDate(),
                command.endDate(),
                unitPrice
        );

        rental.addItem(rentalItem);

        rentalTotalAmount = rentalTotalAmount.add(unitPrice.multiply(BigDecimal.valueOf(rentalDays))
                .setScale(2, RoundingMode.HALF_UP));

        rental.updateTotalAmount(rentalTotalAmount);
        logCreatedEvent(rental);

        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    private void logCreatedEvent(Rental rental) {

        rentalEventLogService.logEvent(rental, RentalEventType.CREATED, Map.of(
                "memberId", rental.getMemberId(),
                "status", rental.getStatus().name(),
                "totalAmount", rental.getTotalAmount(),
                "itemCount", rental.getItems() == null ? 0 : rental.getItems().size()
        ));
    }
}
