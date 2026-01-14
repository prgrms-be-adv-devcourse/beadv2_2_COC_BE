package com.coc.modi.product.event;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.coc.modi.kafka.event.ProductIndexEvent;
import com.coc.modi.kafka.topic.KafkaTopics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaProductIndexEventPublisher {
	
	private final KafkaTemplate<String, Object> kafkaTemplate;
	
	public void publishIndex(Long productId) {
		
		publish(ProductIndexEvent.index(productId));
	}
	
	public void publishDelete(Long productId) {
		
		publish(ProductIndexEvent.delete(productId));
	}
	
	private void publish(ProductIndexEvent event) {
		
		if (event == null || event.productId() == null) {
			return;
		}
		
		try {
			
			log.info("Kafka 이벤트 발행. event=product-index topic={} eventId={} productId={} action={}",
					KafkaTopics.PRODUCT_INDEX_EVENTS, event.eventId(), event.productId(), event.action());
			kafkaTemplate.send(KafkaTopics.PRODUCT_INDEX_EVENTS, event.productId().toString(), event);
		} catch (Exception ex) {
			
			log.warn("Kafka 이벤트 발행 실패. event=product-index eventId={} productId={} action={}",
					event.eventId(), event.productId(), event.action(), ex);
		}
	}
}
