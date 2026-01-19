package com.coc.modi.product.config;

import java.util.Map;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import com.coc.modi.kafka.event.CartItemEvent;

@Configuration
public class KafkaConsumerConfig {
	
	@Bean
	public ConsumerFactory<String, CartItemEvent> cartItemConsumerFactory(KafkaProperties kafkaProperties) {
		
		Map<String, Object> props = kafkaProperties.buildConsumerProperties();
		props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, CartItemEvent.class);
		props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.coc.modi.kafka.event");
		
		return new DefaultKafkaConsumerFactory<>(
				props,
				new StringDeserializer(),
				new JsonDeserializer<>(CartItemEvent.class), false);
	}
	
	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, CartItemEvent> cartItemKafkaListenerContainerFactory(
			ConsumerFactory<String, CartItemEvent> consumerFactory) {
		
		ConcurrentKafkaListenerContainerFactory<String, CartItemEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory);
		
		return factory;
	}
}
