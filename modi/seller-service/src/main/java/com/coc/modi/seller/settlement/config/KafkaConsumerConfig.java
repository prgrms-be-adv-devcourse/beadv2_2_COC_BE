package com.coc.modi.seller.settlement.config;

import java.util.Map;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import com.coc.modi.kafka.event.SettlementPayoutCompletedEvent;
import com.coc.modi.kafka.event.SettlementPayoutFailedEvent;

@Configuration
public class KafkaConsumerConfig {

	@Bean
	public ConsumerFactory<String, SettlementPayoutCompletedEvent> settlementPayoutCompletedConsumerFactory(
			KafkaProperties kafkaProperties
	) {

		Map<String, Object> props = kafkaProperties.buildConsumerProperties();
		props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, SettlementPayoutCompletedEvent.class);
		props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.coc.modi.kafka.event");

		return new DefaultKafkaConsumerFactory<>(
				props,
				new StringDeserializer(),
				new JsonDeserializer<>(SettlementPayoutCompletedEvent.class),
				false
		);
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, SettlementPayoutCompletedEvent>
	settlementPayoutCompletedKafkaListenerContainerFactory(
			ConsumerFactory<String, SettlementPayoutCompletedEvent> consumerFactory
	) {

		ConcurrentKafkaListenerContainerFactory<String, SettlementPayoutCompletedEvent> factory =
				new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory);
		return factory;
	}

	@Bean
	public ConsumerFactory<String, SettlementPayoutFailedEvent> settlementPayoutFailedConsumerFactory(
			KafkaProperties kafkaProperties
	) {

		Map<String, Object> props = kafkaProperties.buildConsumerProperties();
		props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, SettlementPayoutFailedEvent.class);
		props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.coc.modi.kafka.event");

		return new DefaultKafkaConsumerFactory<>(
				props,
				new StringDeserializer(),
				new JsonDeserializer<>(SettlementPayoutFailedEvent.class),
				false
		);
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, SettlementPayoutFailedEvent>
	settlementPayoutFailedKafkaListenerContainerFactory(
			ConsumerFactory<String, SettlementPayoutFailedEvent> consumerFactory
	) {

		ConcurrentKafkaListenerContainerFactory<String, SettlementPayoutFailedEvent> factory =
				new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory);
		return factory;
	}
}
