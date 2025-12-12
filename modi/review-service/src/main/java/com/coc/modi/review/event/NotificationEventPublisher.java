package com.coc.modi.review.event;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.coc.modi.common.NotificationEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationEventPublisher {
	
	private static final String TOPIC_NOTIFICATION = "notification-events";
	
	private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;
	
	public void publish(NotificationEvent event) {
		
		kafkaTemplate.send(
				TOPIC_NOTIFICATION,
				event.getReceiverId().toString(),
				event
		);
	}
}
