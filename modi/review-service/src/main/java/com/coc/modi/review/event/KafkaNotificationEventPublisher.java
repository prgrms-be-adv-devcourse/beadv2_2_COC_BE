package com.coc.modi.review.event;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.coc.modi.kafka.event.NotificationEvent;
import com.coc.modi.kafka.topic.KafkaTopics;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class KafkaNotificationEventPublisher implements NotificationEventPublisher {

	private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;

	@Override
	public void publish(NotificationEvent event) {
		kafkaTemplate.send(KafkaTopics.NOTIFICATION_EVENTS, event.receiverId().toString(), event);
	}
}
