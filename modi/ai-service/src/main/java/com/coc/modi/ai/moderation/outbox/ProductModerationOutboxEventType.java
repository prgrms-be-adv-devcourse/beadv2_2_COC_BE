package com.coc.modi.ai.moderation.outbox;

import com.coc.modi.kafka.topic.KafkaTopics;

public enum ProductModerationOutboxEventType {
	PRODUCT_MODERATION_RESULT(KafkaTopics.PRODUCT_MODERATION_RESULT);

	private final String topic;

	ProductModerationOutboxEventType(String topic) {
		this.topic = topic;
	}

	public String getTopic() {
		return topic;
	}
}
