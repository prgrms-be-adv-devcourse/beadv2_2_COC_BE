package com.coc.modi.member.outbox;

import com.coc.modi.kafka.topic.KafkaTopics;

import lombok.Getter;

@Getter
public enum MemberOutboxEventType {
	MEMBER_CREATED(KafkaTopics.MEMBER_CREATED_EVENTS),
	MEMBER_ROLE_CHANGED(KafkaTopics.MEMBER_ROLE_CHANGED_EVENTS);

	private final String topic;

	MemberOutboxEventType(String topic) {
		this.topic = topic;
	}
	
}
