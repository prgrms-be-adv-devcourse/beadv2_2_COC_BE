package com.coc.modi.member.outbox;

import com.coc.modi.kafka.event.MemberCreatedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberOutboxService {
	
	private final MemberOutboxEventRepository outboxEventRepository;
	private final ObjectMapper objectMapper;
	
	public void enqueueMemberCreated(MemberCreatedEvent event) {
		
		String payload = writePayload(event);
		MemberOutboxEvent outboxEvent = MemberOutboxEvent.create(
				"MEMBER",
				event.memberId(),
				MemberOutboxEventType.MEMBER_CREATED,
				payload
		);
		
		outboxEventRepository.save(outboxEvent);
	}
	
	private String writePayload(MemberCreatedEvent event) {
		
		try {
			return objectMapper.writeValueAsString(event);
		} catch (JsonProcessingException ex) {
			throw new IllegalStateException("Failed to serialize member created event", ex);
		}
	}
}
