package com.coc.modi.seller.outbox;

import com.coc.modi.kafka.event.SellerRegistrationApprovedEvent;
import com.coc.modi.kafka.event.SellerRegistrationRejectedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SellerOutboxService {

	private final SellerOutboxEventRepository outboxEventRepository;
	private final ObjectMapper objectMapper;

	public void enqueueSellerApproved(SellerRegistrationApprovedEvent event) {

		String payload = writePayload(event);
		SellerOutboxEvent outboxEvent = SellerOutboxEvent.create(
				"SELLER_REGISTRATION",
				event.registrationId(),
				SellerOutboxEventType.SELLER_REGISTRATION_APPROVED,
				payload
		);

		outboxEventRepository.save(outboxEvent);
	}

	public void enqueueSellerRejected(SellerRegistrationRejectedEvent event) {

		String payload = writePayload(event);
		SellerOutboxEvent outboxEvent = SellerOutboxEvent.create(
				"SELLER_REGISTRATION",
				event.registrationId(),
				SellerOutboxEventType.SELLER_REGISTRATION_REJECTED,
				payload
		);

		outboxEventRepository.save(outboxEvent);
	}

	private String writePayload(Object event) {

		try {
			return objectMapper.writeValueAsString(event);
		} catch (JsonProcessingException ex) {
			throw new IllegalStateException("Failed to serialize seller event", ex);
		}
	}
}
