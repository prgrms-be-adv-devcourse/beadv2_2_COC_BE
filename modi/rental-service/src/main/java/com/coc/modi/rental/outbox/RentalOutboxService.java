package com.coc.modi.rental.outbox;

import com.coc.modi.kafka.event.CartItemEvent;
import com.coc.modi.kafka.event.NotificationEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RentalOutboxService {
	
	private final RentalOutboxEventRepository outboxEventRepository;
	private final ObjectMapper objectMapper;
	
	public void enqueueNotificationEvent(Long rentalItemId, NotificationEvent event) {
		
		String payload = writePayload(event, "Failed to serialize rental notification event");
		RentalOutboxEvent outboxEvent = RentalOutboxEvent.create(
				"RENTAL_ITEM",
				rentalItemId,
				RentalOutboxEventType.NOTIFICATION_EVENT,
				payload
		);
		
		outboxEventRepository.save(outboxEvent);
	}

	public void enqueueCartItemAdded(Long memberId, Long productId, Long cartItemId) {
		
		CartItemEvent event = CartItemEvent.added(memberId, productId, cartItemId);
		enqueueCartItemEvent(cartItemId, event);
	}

	public void enqueueCartItemRemoved(Long memberId, Long productId, Long cartItemId) {
		
		CartItemEvent event = CartItemEvent.removed(memberId, productId, cartItemId);
		enqueueCartItemEvent(cartItemId, event);
	}

	private void enqueueCartItemEvent(Long cartItemId, CartItemEvent event) {
		
		String payload = writePayload(event, "Failed to serialize cart item event");
		RentalOutboxEvent outboxEvent = RentalOutboxEvent.create(
				"CART_ITEM",
				cartItemId,
				RentalOutboxEventType.CART_ITEM_EVENT,
				payload
		);
		
		outboxEventRepository.save(outboxEvent);
	}
	
	private String writePayload(Object event, String errorMessage) {
		
		try {
			return objectMapper.writeValueAsString(event);
		} catch (JsonProcessingException ex) {
			throw new IllegalStateException(errorMessage, ex);
		}
	}
}
