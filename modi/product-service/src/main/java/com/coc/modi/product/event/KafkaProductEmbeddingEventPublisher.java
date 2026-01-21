package com.coc.modi.product.event;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.coc.modi.kafka.event.ProductEmbeddingEvent;
import com.coc.modi.kafka.topic.KafkaTopics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static net.logstash.logback.argument.StructuredArguments.kv;

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
			
			log.info("kafka_event_publish",
					kv("event.name", "product_embedding"),
					kv("kafka.topic", KafkaTopics.PRODUCT_EMBEDDING_EVENTS),
					kv("event.id", event.eventId()),
					kv("product.id", productId),
					kv("event.action", event.action()));
			kafkaTemplate.send(KafkaTopics.PRODUCT_EMBEDDING_EVENTS, productId.toString(), event);
		} catch (Exception ex) {
			
			log.warn("kafka_event_publish_failed",
					kv("event.name", "product_embedding"),
					kv("event.id", event.eventId()),
					kv("product.id", productId),
					kv("event.action", event.action()),
					kv("exception.class", ex.getClass().getName()),
					ex);
		}
	}
}
