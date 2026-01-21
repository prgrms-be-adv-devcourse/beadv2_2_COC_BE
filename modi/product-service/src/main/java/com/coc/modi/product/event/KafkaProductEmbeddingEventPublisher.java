package com.coc.modi.product.event;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.coc.modi.kafka.event.ProductEmbeddingEvent;
import com.coc.modi.kafka.topic.KafkaTopics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaProductEmbeddingEventPublisher {
	
	private final KafkaTemplate<String, Object> kafkaTemplate;
	
	public void publishUpdate(Long productId) {
		
		if (productId == null) {
			return;
		}
		
		ProductEmbeddingEvent event = ProductEmbeddingEvent.update(productId);
		try {
			
			log.info("Kafka 이벤트 발행. event=product-embedding topic={} eventId={} productId={} action={}",
					KafkaTopics.PRODUCT_EMBEDDING_EVENTS, event.eventId(), productId, event.action());
			kafkaTemplate.send(KafkaTopics.PRODUCT_EMBEDDING_EVENTS, productId.toString(), event);
		} catch (Exception ex) {
			
			log.warn("Kafka 이벤트 발행 실패. event=product-embedding eventId={} productId={} action={}",
					event.eventId(), productId, event.action(), ex);
		}
	}
}
