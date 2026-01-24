package com.coc.modi.ai.moderation.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.coc.modi.ai.moderation.application.ProductModerationService;
import com.coc.modi.kafka.event.ProductModerationRequestedEvent;
import com.coc.modi.kafka.topic.KafkaTopics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductModerationRequestedEventListener {

	private final ProductModerationService productModerationService;

	@KafkaListener(
			topics = KafkaTopics.PRODUCT_MODERATION_REQUESTED,
			groupId = "ai-product-moderation",
			containerFactory = "productModerationKafkaListenerContainerFactory"
	)
	public void onModerationRequested(ProductModerationRequestedEvent event) {

		if (event == null || event.productId() == null) {
			log.warn("Kafka event skipped. reason=missing-data event=product-moderation-requested eventId={} productId={}",
					event != null ? event.eventId() : null,
					event != null ? event.productId() : null);
			return;
		}

		log.info("Kafka event received. event=product-moderation-requested topic={} eventId={} productId={}",
				KafkaTopics.PRODUCT_MODERATION_REQUESTED, event.eventId(), event.productId());
		productModerationService.handle(event);
	}
}
