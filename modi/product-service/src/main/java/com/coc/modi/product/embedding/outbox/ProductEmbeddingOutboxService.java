package com.coc.modi.product.embedding.outbox;

import com.coc.modi.kafka.event.ProductEmbeddingEvent;
import com.coc.modi.product.product.exception.ProductEmbeddingOutboxException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductEmbeddingOutboxService {
	
	private final ProductEmbeddingOutboxEventRepository outboxEventRepository;
	private final ObjectMapper objectMapper;
	
	@Transactional
	public void enqueueUpdate(Long productId) {
		ProductEmbeddingEvent event = ProductEmbeddingEvent.update(productId);
		ProductEmbeddingOutboxEvent outboxEvent = ProductEmbeddingOutboxEvent.create(
				"PRODUCT",
				productId,
				ProductEmbeddingOutboxEventType.PRODUCT_EMBEDDING_EVENT,
				writePayload(event)
		);
		outboxEventRepository.save(outboxEvent);
	}
	
	private String writePayload(Object payload) {
		try {
			return objectMapper.writeValueAsString(payload);
		} catch (JsonProcessingException ex) {
			throw new ProductEmbeddingOutboxException("아웃박스 payload 직렬화에 실패했습니다.", ex);
		}
	}
}
