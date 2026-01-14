package com.coc.modi.review.outbox;

import com.coc.modi.kafka.event.NotificationEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewOutboxService {
	
	private final ReviewOutboxEventRepository outboxEventRepository;
	private final ObjectMapper objectMapper;
	
	public void enqueueNotificationEvent(Long reviewId, NotificationEvent event) {
		
		String payload = writePayload(event);
		ReviewOutboxEvent outboxEvent = ReviewOutboxEvent.create(
				"REVIEW",
				reviewId,
				ReviewOutboxEventType.NOTIFICATION_EVENT,
				payload
		);
		
		outboxEventRepository.save(outboxEvent);
	}
	
	private String writePayload(NotificationEvent event) {
		
		try {
			return objectMapper.writeValueAsString(event);
		} catch (JsonProcessingException ex) {
			throw new IllegalStateException("Failed to serialize review notification event", ex);
		}
	}
}
