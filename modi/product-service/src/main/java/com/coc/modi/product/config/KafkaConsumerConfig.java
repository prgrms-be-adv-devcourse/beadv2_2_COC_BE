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

import com.coc.modi.kafka.event.ProductEmbeddingEvent;
import com.coc.modi.kafka.event.ProductIndexEvent;

@Configuration
public class KafkaConsumerConfig {
	
	@Bean
	public ConsumerFactory<String, ProductIndexEvent> productIndexConsumerFactory(KafkaProperties kafkaProperties) {
		
		Map<String, Object> props = kafkaProperties.buildConsumerProperties();
		props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, ProductIndexEvent.class);
		props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.coc.modi.kafka.event");
		
		return new DefaultKafkaConsumerFactory<>(
				props,
				new StringDeserializer(),
				new JsonDeserializer<>(ProductIndexEvent.class), false);
	}
	
	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, ProductIndexEvent> productIndexKafkaListenerContainerFactory(
			ConsumerFactory<String, ProductIndexEvent> consumerFactory) {
		
		ConcurrentKafkaListenerContainerFactory<String, ProductIndexEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory);
		return factory;
	}
	
	@Bean
	public ConsumerFactory<String, ProductEmbeddingEvent> productEmbeddingConsumerFactory(KafkaProperties kafkaProperties) {
		
		Map<String, Object> props = kafkaProperties.buildConsumerProperties();
		props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, ProductEmbeddingEvent.class);
		props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.coc.modi.kafka.event");
		
		return new DefaultKafkaConsumerFactory<>(
				props,
				new StringDeserializer(),
				new JsonDeserializer<>(ProductEmbeddingEvent.class), false);
	}
	
	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, ProductEmbeddingEvent> productEmbeddingKafkaListenerContainerFactory(
			ConsumerFactory<String, ProductEmbeddingEvent> consumerFactory) {
		
		ConcurrentKafkaListenerContainerFactory<String, ProductEmbeddingEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory);
		return factory;
	}
}
