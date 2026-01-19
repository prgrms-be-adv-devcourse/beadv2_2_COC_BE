package com.coc.modi.product.product.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.coc.modi.kafka.event.ProductModerationResultEvent;
import com.coc.modi.kafka.topic.KafkaTopics;
import com.coc.modi.product.product.application.ProductModerationResultService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductModerationResultEventListener {

	private final ProductModerationResultService productModerationResultService;

	@KafkaListener(
			topics = KafkaTopics.PRODUCT_MODERATION_RESULT,
			groupId = "product-service",
			containerFactory = "productModerationResultKafkaListenerContainerFactory"
	)
	public void onModerationResult(ProductModerationResultEvent event) {

		productModerationResultService.handle(event);
	}
}
