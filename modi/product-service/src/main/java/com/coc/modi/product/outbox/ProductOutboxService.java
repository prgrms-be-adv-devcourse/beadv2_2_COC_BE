package com.coc.modi.product.outbox;

import com.coc.modi.kafka.event.ProductModerationRequestedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductOutboxService {

	private final ProductOutboxEventRepository outboxEventRepository;
	private final ObjectMapper objectMapper;

	public void enqueueModerationRequested(Long productId, ProductModerationRequestedEvent event) {

		String payload = writePayload(event);
		ProductOutboxEvent outboxEvent = ProductOutboxEvent.create(
				"PRODUCT",
				productId,
				ProductOutboxEventType.PRODUCT_MODERATION_REQUESTED,
				payload
		);

		outboxEventRepository.save(outboxEvent);
	}

	private String writePayload(ProductModerationRequestedEvent event) {

		try {
			return objectMapper.writeValueAsString(event);
		} catch (JsonProcessingException ex) {
			throw new IllegalStateException("Failed to serialize product moderation request event", ex);
		}
	}
}
