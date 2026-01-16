package com.coc.modi.rental.outbox;

import com.coc.modi.kafka.topic.KafkaTopics;

import lombok.Getter;

@Getter
public enum RentalOutboxEventType {
	NOTIFICATION_EVENT(KafkaTopics.NOTIFICATION_EVENTS),
	RENTAL_RETURNED_EVENT(KafkaTopics.RENTAL_RETURNED_EVENTS);

	private final String topic;

	RentalOutboxEventType(String topic) {
		this.topic = topic;
	}
	
}
