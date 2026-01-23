package com.coc.modi.product.viewlog.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.coc.modi.kafka.event.CartItemEvent;
import com.coc.modi.kafka.event.CartItemEventAction;
import com.coc.modi.kafka.topic.KafkaTopics;
import com.coc.modi.product.viewlog.application.ProductViewService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartItemEventListener {
	
	private final ProductViewService productViewService;
	
	@KafkaListener(
			topics = KafkaTopics.CART_ITEM_EVENTS,
			groupId = "product-service",
			containerFactory = "cartItemKafkaListenerContainerFactory"
	)
	public void onCartItemEvent(CartItemEvent event) {
		
		if (event == null || event.memberId() == null || event.productId() == null) {
			return;
		}
		if (event.action() == CartItemEventAction.ADDED) {
			productViewService.updateAddedToCart(event.memberId(), event.productId(), true);
			return;
		}
		if (event.action() == CartItemEventAction.REMOVED) {
			productViewService.updateAddedToCart(event.memberId(), event.productId(), false);
		}
	}
}
