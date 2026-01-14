package com.coc.modi.product.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.coc.modi.kafka.event.ProductIndexEvent;
import com.coc.modi.kafka.topic.KafkaTopics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductEventListener {

	private final ProductEventHandler productEventHandler;

	@KafkaListener(
			topics = KafkaTopics.PRODUCT_INDEX_EVENTS,
			groupId = "product-indexer",
			containerFactory = "productIndexKafkaListenerContainerFactory"
	)
	public void onProductIndexEvent(ProductIndexEvent event) {
		if (event == null || event.productId() == null || event.action() == null) {
			log.warn("Kafka 이벤트 건너뜀. reason=missing-data event=product-index eventId={} productId={} action={}",
					event != null ? event.eventId() : null,
					event != null ? event.productId() : null,
					event != null ? event.action() : null);
			return;
		}

		log.info("Kafka 이벤트 수신. event=product-index topic={} eventId={} productId={} action={}",
				KafkaTopics.PRODUCT_INDEX_EVENTS, event.eventId(), event.productId(), event.action());
		productEventHandler.handle(event);
	}

}
