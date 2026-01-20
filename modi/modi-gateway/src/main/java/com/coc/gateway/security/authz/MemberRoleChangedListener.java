package com.coc.gateway.security.authz;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.coc.modi.kafka.event.MemberRoleChangedEvent;
import com.coc.modi.kafka.topic.KafkaTopics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberRoleChangedListener {

	private final MemberAuthzService memberAuthzService;

	@KafkaListener(
			topics = KafkaTopics.MEMBER_ROLE_CHANGED_EVENTS,
			groupId = "${gateway.authz.kafka-group-id:modi-gateway-authz}",
			containerFactory = "memberRoleChangedKafkaListenerContainerFactory"
	)
	public void onMemberRoleChanged(MemberRoleChangedEvent event) {
		if (event == null || event.memberId() == null) {
			return;
		}

		memberAuthzService.evictMember(event.memberId()).subscribe();
		log.info("authz cache evicted for memberId={}, role={}", event.memberId(), event.role());
	}
}
