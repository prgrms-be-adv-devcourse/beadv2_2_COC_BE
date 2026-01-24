package com.coc.modi.product.outbox;

import com.coc.modi.kafka.topic.KafkaTopics;

public enum ProductOutboxEventType {
	PRODUCT_MODERATION_REQUESTED(KafkaTopics.PRODUCT_MODERATION_REQUESTED),
	NOTIFICATION_EVENT(KafkaTopics.NOTIFICATION_EVENTS),
	PRODUCT_EMBEDDING_EVENT(KafkaTopics.PRODUCT_EMBEDDING_EVENTS);

	private final String topic;

	ProductOutboxEventType(String topic) {
		this.topic = topic;
	}

	public String getTopic() {
		return topic;
	}
}
