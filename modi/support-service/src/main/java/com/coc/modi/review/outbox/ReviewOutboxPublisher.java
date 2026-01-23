package com.coc.modi.review.outbox;

import java.util.List;

import com.coc.modi.kafka.event.NotificationEvent;
import com.coc.modi.kafka.event.ReviewSummaryRequestEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewOutboxPublisher {
	
	private final ReviewOutboxEventRepository outboxEventRepository;
	private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;
	private final KafkaTemplate<String, Object> kafkaTemplateObject;
	private final ObjectMapper objectMapper;
	
	@Value("${outbox.publisher.batch-size:50}")
	private int batchSize;
	
	@Value("${outbox.publisher.max-retries:10}")
	private int maxRetries;
	
	@Scheduled(fixedDelayString = "${outbox.publisher.delay-ms:1000}")
	@Transactional
	public void publishPendingEvents() {
		
		List<ReviewOutboxEvent> events = outboxEventRepository.findPendingForPublish(batchSize);
		
		for (ReviewOutboxEvent event : events) {
			try {
				publishEvent(event);
				event.markSent();
			} catch (Exception ex) {
				event.markFailed(ex.getMessage(), maxRetries);
				log.warn("Outbox publish failed. id={}, type={}, retryCount={}",
						event.getId(), event.getEventType(), event.getRetryCount(), ex);
			}
		}
	}
	
	private void publishEvent(ReviewOutboxEvent event) throws Exception {
		
		if (event.getEventType() == ReviewOutboxEventType.NOTIFICATION_EVENT) {
			NotificationEvent payload = readPayload(event.getPayload(), NotificationEvent.class);
			kafkaTemplate
					.send(event.getEventType().getTopic(), payload.receiverId().toString(), payload)
					.get();
			return;
		}
		if (event.getEventType() == ReviewOutboxEventType.REVIEW_SUMMARY_REQUEST) {
			ReviewSummaryRequestEvent payload = readPayload(event.getPayload(), ReviewSummaryRequestEvent.class);
			kafkaTemplateObject
					.send(event.getEventType().getTopic(), payload.sellerId().toString(), payload)
					.get();
			return;
		}
		
		throw new IllegalStateException("Unsupported outbox event type: " + event.getEventType());
	}
	
	private <T> T readPayload(String payload, Class<T> target) {
		
		try {
			return objectMapper.readValue(payload, target);
		} catch (JsonProcessingException ex) {
			throw new IllegalStateException("Failed to deserialize outbox payload", ex);
		}
	}
}
