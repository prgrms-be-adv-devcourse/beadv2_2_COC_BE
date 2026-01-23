package com.coc.modi.review.config;

import java.util.Map;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import com.coc.modi.kafka.event.RentalReturnedEvent;
import com.coc.modi.kafka.event.ReviewSummaryResultEvent;

@Configuration
public class ReviewKafkaConsumerConfig {

	@Bean
	public ConsumerFactory<String, RentalReturnedEvent> rentalReturnedConsumerFactory(KafkaProperties kafkaProperties) {
		Map<String, Object> props = kafkaProperties.buildConsumerProperties();
		props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, RentalReturnedEvent.class);
		props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.coc.modi.kafka.event");

		return new DefaultKafkaConsumerFactory<>(
				props,
				new StringDeserializer(),
				new JsonDeserializer<>(RentalReturnedEvent.class), false);
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, RentalReturnedEvent> rentalReturnedKafkaListenerContainerFactory(
			ConsumerFactory<String, RentalReturnedEvent> consumerFactory) {

		ConcurrentKafkaListenerContainerFactory<String, RentalReturnedEvent> factory =
				new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory);

		return factory;
	}

	@Bean
	public ConsumerFactory<String, ReviewSummaryResultEvent> reviewSummaryResultConsumerFactory(KafkaProperties kafkaProperties) {
		Map<String, Object> props = kafkaProperties.buildConsumerProperties();
		props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, ReviewSummaryResultEvent.class);
		props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.coc.modi.kafka.event");

		return new DefaultKafkaConsumerFactory<>(
				props,
				new StringDeserializer(),
				new JsonDeserializer<>(ReviewSummaryResultEvent.class), false);
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, ReviewSummaryResultEvent> reviewSummaryResultKafkaListenerContainerFactory(
			ConsumerFactory<String, ReviewSummaryResultEvent> consumerFactory) {

		ConcurrentKafkaListenerContainerFactory<String, ReviewSummaryResultEvent> factory =
				new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory);

		return factory;
	}
}
