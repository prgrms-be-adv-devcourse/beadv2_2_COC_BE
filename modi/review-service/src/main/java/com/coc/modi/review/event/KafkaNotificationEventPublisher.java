package com.coc.modi.review.event;

import org.springframework.stereotype.Component;

import com.coc.modi.kafka.event.NotificationEvent;
import com.coc.modi.review.outbox.ReviewOutboxService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class KafkaNotificationEventPublisher implements NotificationEventPublisher {

	private final ReviewOutboxService reviewOutboxService;

	@Override
	public void publish(Long reviewId, NotificationEvent event) {
		reviewOutboxService.enqueueNotificationEvent(reviewId, event);
	}
}
