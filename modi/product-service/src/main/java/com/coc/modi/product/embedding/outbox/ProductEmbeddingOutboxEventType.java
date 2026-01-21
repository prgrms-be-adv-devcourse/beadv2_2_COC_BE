package com.coc.modi.product.embedding.outbox;

import com.coc.modi.kafka.topic.KafkaTopics;

public enum ProductEmbeddingOutboxEventType {
	
	PRODUCT_EMBEDDING_EVENT(KafkaTopics.PRODUCT_EMBEDDING_EVENTS);
	
	private final String topic;
	
	ProductEmbeddingOutboxEventType(String topic) {
		this.topic = topic;
	}
	
	public String getTopic() {
		return topic;
	}
}
