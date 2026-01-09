package com.coc.modi.seller.outbox;

import com.coc.modi.kafka.topic.KafkaTopics;

public enum SellerOutboxEventType {
	SELLER_APPROVED(KafkaTopics.SELLER_APPROVED),
	SELLER_REJECTED(KafkaTopics.SELLER_REJECTED);

	private final String topic;

	SellerOutboxEventType(String topic) {
		this.topic = topic;
	}

	public String getTopic() {
		return topic;
	}
}
