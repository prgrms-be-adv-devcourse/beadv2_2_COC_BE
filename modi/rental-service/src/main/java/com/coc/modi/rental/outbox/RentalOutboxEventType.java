package com.coc.modi.rental.outbox;

import com.coc.modi.kafka.topic.KafkaTopics;

public enum RentalOutboxEventType {
	NOTIFICATION_EVENT(KafkaTopics.NOTIFICATION_EVENTS);

	private final String topic;

	RentalOutboxEventType(String topic) {
		this.topic = topic;
	}

	public String getTopic() {
		return topic;
	}
}
