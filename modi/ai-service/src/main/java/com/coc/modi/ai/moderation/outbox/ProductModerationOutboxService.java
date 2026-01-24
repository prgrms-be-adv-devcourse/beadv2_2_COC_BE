package com.coc.modi.ai.moderation.outbox;

import com.coc.modi.kafka.event.ProductModerationResultEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductModerationOutboxService {

	private final ProductModerationOutboxEventRepository outboxEventRepository;
	private final ObjectMapper objectMapper;

	public void enqueueModerationResult(Long productId, ProductModerationResultEvent event) {

		String payload = writePayload(event);
		ProductModerationOutboxEvent outboxEvent = ProductModerationOutboxEvent.create(
				"PRODUCT",
				productId,
				ProductModerationOutboxEventType.PRODUCT_MODERATION_RESULT,
				payload
		);

		outboxEventRepository.save(outboxEvent);
	}

	private String writePayload(ProductModerationResultEvent event) {

		try {
			return objectMapper.writeValueAsString(event);
		} catch (JsonProcessingException ex) {
			throw new IllegalStateException("Failed to serialize product moderation result event", ex);
		}
	}
}
