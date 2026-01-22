package com.coc.modi.seller.outbox;

import com.coc.modi.kafka.topic.KafkaTopics;

public enum SellerOutboxEventType {
	SELLER_REGISTRATION_APPROVED(KafkaTopics.SELLER_REGISTRATION_APPROVED),
	SELLER_REGISTRATION_REJECTED(KafkaTopics.SELLER_REGISTRATION_REJECTED);

	private final String topic;

	SellerOutboxEventType(String topic) {
		this.topic = topic;
	}

	public String getTopic() {
		return topic;
	}
}
