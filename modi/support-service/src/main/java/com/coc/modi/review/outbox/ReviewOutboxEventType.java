package com.coc.modi.review.outbox;

import com.coc.modi.kafka.topic.KafkaTopics;
import lombok.Getter;

@Getter
public enum ReviewOutboxEventType {
	NOTIFICATION_EVENT(KafkaTopics.NOTIFICATION_EVENTS),
	REVIEW_SUMMARY_REQUEST(KafkaTopics.REVIEW_SUMMARY_REQUEST_EVENTS);

	private final String topic;

	ReviewOutboxEventType(String topic) {
		this.topic = topic;
	}

}
