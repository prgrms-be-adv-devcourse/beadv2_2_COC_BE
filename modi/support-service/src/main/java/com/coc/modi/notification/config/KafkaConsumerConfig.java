package com.coc.modi.notification.config;

import java.util.Map;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import com.coc.modi.kafka.event.NotificationEvent;
import com.coc.modi.kafka.event.SellerRegistrationApprovedEvent;
import com.coc.modi.kafka.event.SellerRegistrationRejectedEvent;

@Configuration
public class KafkaConsumerConfig {
	
	@Bean
	public ConsumerFactory<String, NotificationEvent> notificationConsumerFactory(KafkaProperties kafkaProperties) {
		
		Map<String, Object> props = kafkaProperties.buildConsumerProperties();
		props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, NotificationEvent.class);
		props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.coc.modi.kafka.event");
		
		return new DefaultKafkaConsumerFactory<>(
				props,
				new StringDeserializer(),
				new JsonDeserializer<>(NotificationEvent.class), false);
	}
	
	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, NotificationEvent> notificationKafkaListenerContainerFactory(
			ConsumerFactory<String, NotificationEvent> consumerFactory) {
		
		ConcurrentKafkaListenerContainerFactory<String, NotificationEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory);
		
		return factory;
	}

	@Bean
	public ConsumerFactory<String, SellerRegistrationApprovedEvent> sellerRegistrationApprovedConsumerFactory(
			KafkaProperties kafkaProperties) {

		Map<String, Object> props = kafkaProperties.buildConsumerProperties();
		props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, SellerRegistrationApprovedEvent.class);
		props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.coc.modi.kafka.event");

		return new DefaultKafkaConsumerFactory<>(
				props,
				new StringDeserializer(),
				new JsonDeserializer<>(SellerRegistrationApprovedEvent.class), false);
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, SellerRegistrationApprovedEvent>
			sellerRegistrationApprovedKafkaListenerContainerFactory(
					ConsumerFactory<String, SellerRegistrationApprovedEvent> consumerFactory) {

		ConcurrentKafkaListenerContainerFactory<String, SellerRegistrationApprovedEvent> factory =
				new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory);

		return factory;
	}

	@Bean
	public ConsumerFactory<String, SellerRegistrationRejectedEvent> sellerRegistrationRejectedConsumerFactory(
			KafkaProperties kafkaProperties) {

		Map<String, Object> props = kafkaProperties.buildConsumerProperties();
		props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, SellerRegistrationRejectedEvent.class);
		props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.coc.modi.kafka.event");

		return new DefaultKafkaConsumerFactory<>(
				props,
				new StringDeserializer(),
				new JsonDeserializer<>(SellerRegistrationRejectedEvent.class), false);
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, SellerRegistrationRejectedEvent>
			sellerRegistrationRejectedKafkaListenerContainerFactory(
					ConsumerFactory<String, SellerRegistrationRejectedEvent> consumerFactory) {

		ConcurrentKafkaListenerContainerFactory<String, SellerRegistrationRejectedEvent> factory =
				new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory);

		return factory;
	}
}
