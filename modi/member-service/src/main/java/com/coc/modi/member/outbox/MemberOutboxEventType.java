package com.coc.modi.member.outbox;

import com.coc.modi.kafka.topic.KafkaTopics;

public enum MemberOutboxEventType {
	MEMBER_CREATED(KafkaTopics.MEMBER_CREATED),
	MEMBER_ROLE_CHANGED(KafkaTopics.MEMBER_ROLE_CHANGED);

	private final String topic;

	MemberOutboxEventType(String topic) {
		this.topic = topic;
	}

	public String getTopic() {
		return topic;
	}
}
