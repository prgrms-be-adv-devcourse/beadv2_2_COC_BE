package com.coc.modi.rental.outbox;

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
		
		String payload = writePayload(event);
		RentalOutboxEvent outboxEvent = RentalOutboxEvent.create(
				"RENTAL_ITEM",
				rentalItemId,
				RentalOutboxEventType.NOTIFICATION_EVENT,
				payload
		);
		
		outboxEventRepository.save(outboxEvent);
	}
	
	private String writePayload(NotificationEvent event) {
		
		try {
			return objectMapper.writeValueAsString(event);
		} catch (JsonProcessingException ex) {
			throw new IllegalStateException("Failed to serialize rental notification event", ex);
		}
	}
}
