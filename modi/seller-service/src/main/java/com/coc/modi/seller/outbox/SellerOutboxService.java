package com.coc.modi.seller.outbox;

import com.coc.modi.kafka.event.SellerApprovedEvent;
import com.coc.modi.kafka.event.SellerRejectedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SellerOutboxService {

	private final SellerOutboxEventRepository outboxEventRepository;
	private final ObjectMapper objectMapper;

	public void enqueueSellerApproved(SellerApprovedEvent event) {

		String payload = writePayload(event);
		SellerOutboxEvent outboxEvent = SellerOutboxEvent.create(
				"SELLER",
				event.sellerId(),
				SellerOutboxEventType.SELLER_APPROVED,
				payload
		);

		outboxEventRepository.save(outboxEvent);
	}

	public void enqueueSellerRejected(SellerRejectedEvent event) {

		String payload = writePayload(event);
		SellerOutboxEvent outboxEvent = SellerOutboxEvent.create(
				"SELLER",
				event.sellerId(),
				SellerOutboxEventType.SELLER_REJECTED,
				payload
		);

		outboxEventRepository.save(outboxEvent);
	}

	private String writePayload(Object event) {

		try {
			return objectMapper.writeValueAsString(event);
		} catch (JsonProcessingException ex) {
			throw new IllegalStateException("Failed to serialize seller approved event", ex);
		}
	}
}
