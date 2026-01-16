package com.coc.modi.review.outbox;

import com.coc.modi.kafka.topic.KafkaTopics;

public enum ReviewOutboxEventType {
	NOTIFICATION_EVENT(KafkaTopics.NOTIFICATION_EVENTS);

	private final String topic;

	ReviewOutboxEventType(String topic) {
		this.topic = topic;
	}

	public String getTopic() {
		return topic;
	}
}
