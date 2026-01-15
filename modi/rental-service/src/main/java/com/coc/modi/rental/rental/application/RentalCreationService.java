package com.coc.modi.rental.rental.application;

import com.coc.modi.common.ErrorCode;
import com.coc.modi.rental.cart.domain.Cart;
import com.coc.modi.rental.cart.domain.CartItem;
import com.coc.modi.rental.cart.domain.CartRepository;
import com.coc.modi.rental.rental.application.dto.CreateRentalFromCartCommand;
import com.coc.modi.rental.rental.application.dto.RentalCreateCommand;
import com.coc.modi.rental.rental.domain.Rental;
import com.coc.modi.rental.rental.domain.RentalEventType;
import com.coc.modi.rental.rental.domain.RentalItem;
import com.coc.modi.rental.rental.domain.RentalQueryRepository;
import com.coc.modi.rental.rental.domain.RentalRepository;
import com.coc.modi.rental.rental.exception.RentalException;
import com.coc.modi.rental.rental.infrastructure.client.ProductClientAdapter;
import com.coc.modi.rental.rental.infrastructure.client.dto.ProductResponseDto;
import com.coc.modi.kafka.event.NotificationEvent;
import com.coc.modi.rental.outbox.RentalOutboxService;

import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RentalCreationService {
	
	private final RentalRepository rentalRepository;
	private final CartRepository cartRepository;
	private final ProductClientAdapter productClientAdapter;
	private final RentalEventLogService rentalEventLogService;
	private final RentalQueryRepository rentalQueryRepository;
	private final RentalOutboxService rentalOutboxService;
	
	@Transactional
	public void createRentalFromCart(CreateRentalFromCartCommand command) {
		
		Cart cart = cartRepository.findByMemberId(command.memberId())
				.orElseThrow(() -> new RentalException(ErrorCode.INVALID_INPUT, "장바구니가 존재하지 않습니다."));
		
		List<CartItem> cartItems = cart.getItems().stream()
				.filter(item -> command.cartItemIds().contains(item.getId()))
				.toList();
		
		if (cartItems.isEmpty()) {
			
			throw new RentalException(ErrorCode.INVALID_INPUT, "요청한 장바구니 항목이 존재하지 않습니다.");
		}
		
		if (cartItems.size() != command.cartItemIds().size()) {
			
			throw new RentalException(ErrorCode.INVALID_INPUT, "요청한 장바구니 항목 일부를 찾을 수 없습니다.");
		}
		
		List<Long> productIds = cartItems.stream().map(CartItem::getProductId).distinct().toList();
		Map<Long, ProductResponseDto> productMap = fetchProducts(productIds).stream()
				.collect(Collectors.toMap(ProductResponseDto::productId, Function.identity()));
		
		Rental rental = Rental.create(command.memberId(), BigDecimal.ZERO);
		
		BigDecimal total = BigDecimal.ZERO;
		
		for (CartItem cartItem : cartItems) {
			
			ProductResponseDto product = productMap.get(cartItem.getProductId());
			
			if (product == null) {
				
				throw new RentalException(ErrorCode.NOT_FOUND,
						"상품 정보를 찾을 수 없습니다. productId=" + cartItem.getProductId());
			}
			
			// 상품 상태 정책(문자열이면 최소한 상수화 추천)
			if (!"ACTIVE".equals(product.status())) {
				throw new RentalException(ErrorCode.CONFLICT, "현재 판매중인 상품이 아닙니다. productId=" + cartItem.getProductId());
			}
			
			validateRentalPeriod(cartItem.getStartDate(), cartItem.getEndDate(), "cartItemId=" + cartItem.getId());
			validateAvailability(cartItem.getProductId(), cartItem.getStartDate(), cartItem.getEndDate());
			
			RentalItem rentalItem = RentalItem.create(
					cartItem.getProductId(),
					product.sellerId(),
					cartItem.getStartDate(),
					cartItem.getEndDate(),
					product.price()
			);
			
			rental.addItem(rentalItem);
			total = total.add(rentalItem.calculateRentalAmount());
		}
		
		rental.updateTotalAmount(total);
		try {
			rentalRepository.saveAndFlush(rental);
		} catch (DataIntegrityViolationException ex) {
			throw new RentalException(ErrorCode.CONFLICT, "해당 기간에 이미 예약된 상품이 포함되어 있습니다.", ex);
		}
		
		rentalEventLogService.logEvent(rental, RentalEventType.CREATED,
				Map.of("memberId", rental.getMemberId(),
						"status", rental.getStatus().name(),
						"totalAmount", rental.getTotalAmount(),
						"itemCount", rental.getItems() == null ? 0 : rental.getItems().size()));
		enqueueRentalRequestedNotifications(rental);
	}
	
	@Transactional
	public void createRental(RentalCreateCommand command) {
		
		ProductResponseDto product = fetchProducts(List.of(command.productId())).stream().findFirst()
				.orElseThrow(() -> new RentalException(ErrorCode.NOT_FOUND,
						"상품 정보를 찾을 수 없습니다. productId=" + command.productId()));
		
		if (!"ACTIVE".equals(product.status())) {
			throw new RentalException(ErrorCode.CONFLICT, "현재 판매중인 상품이 아닙니다. productId=" + command.productId());
		}
		
		validateRentalPeriod(command.startDate(), command.endDate(), "productId=" + command.productId());
		validateAvailability(command.productId(), command.startDate(), command.endDate());
		
		Rental rental = Rental.create(command.memberId(), BigDecimal.ZERO);
		
		RentalItem rentalItem = RentalItem.create(
				command.productId(),
				product.sellerId(),
				command.startDate(),
				command.endDate(),
				product.price()
		);
		
		rental.addItem(rentalItem);
		rental.updateTotalAmount(rentalItem.calculateRentalAmount());
		try {
			rentalRepository.saveAndFlush(rental);
		} catch (DataIntegrityViolationException ex) {
			throw new RentalException(ErrorCode.CONFLICT,
					"해당 기간에 이미 예약된 상품입니다. productId=" + command.productId()
							+ ", startDate=" + command.startDate() + ", endDate=" + command.endDate(), ex);
		}
		
		rentalEventLogService.logEvent(rental, RentalEventType.CREATED,
				Map.of("memberId", rental.getMemberId(),
						"status", rental.getStatus().name(),
						"totalAmount", rental.getTotalAmount(),
						"itemCount", rental.getItems() == null ? 0 : rental.getItems().size()));
		enqueueRentalRequestedNotifications(rental);
	}
	
	private List<ProductResponseDto> fetchProducts(List<Long> productIds) {
		
		return productClientAdapter.getProducts(productIds);
	}
	
	private void validateRentalPeriod(LocalDate startDate, LocalDate endDate, String ref) {
		
		long rentalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
		
		if (rentalDays <= 0) {
			throw new RentalException(ErrorCode.INVALID_INPUT, "대여 종료일이 시작일보다 빠릅니다. " + ref);
		}
	}
	
	private void validateAvailability(Long productId, LocalDate startDate, LocalDate endDate) {
		
		boolean hasOverlap = rentalQueryRepository.existsOverlappingRentalItem(productId, startDate, endDate, null);
		
		if (hasOverlap) {
			throw new RentalException(ErrorCode.CONFLICT,
					"해당 기간에 이미 예약된 상품입니다. productId=" + productId + ", startDate=" + startDate + ", endDate="
							+ endDate);
		}
	}
	
	private void enqueueRentalRequestedNotifications(Rental rental) {
		
		if (rental == null || rental.getItems() == null || rental.getItems().isEmpty()) {
			return;
		}
		
		for (RentalItem item : rental.getItems()) {
			if (item == null) {
				continue;
			}
			if (item.getId() == null) {
				throw new IllegalStateException("Rental item id is required to enqueue notification.");
			}
			
			NotificationEvent event = NotificationEvent.of(
					item.getSellerId(),
					"RENTAL_REQUESTED",
					"새 대여 요청이 도착했습니다!",
					"대여 요청을 확인해주세요.",
					"RENTAL_ITEM",
					String.valueOf(item.getId())
			);
			
			rentalOutboxService.enqueueNotificationEvent(item.getId(), event);
		}
	}
}
