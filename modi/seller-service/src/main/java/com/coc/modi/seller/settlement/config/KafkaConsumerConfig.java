package com.coc.modi.seller.settlement.config;

import java.util.Map;

import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import com.coc.modi.kafka.event.RentalReturnedEvent;
import com.coc.modi.kafka.event.SettlementPayoutCompletedEvent;
import com.coc.modi.kafka.event.SettlementPayoutFailedEvent;

@Configuration
public class KafkaConsumerConfig {

	@Bean
	public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, Object> kafkaTemplate) {

		DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
				kafkaTemplate,
				(record, ex) -> new TopicPartition(record.topic() + ".DLT", record.partition())
		);
		FixedBackOff backOff = new FixedBackOff(1000L, 3L);
		return new DefaultErrorHandler(recoverer, backOff);
	}

	@Bean
	public ConsumerFactory<String, String> dltConsumerFactory(KafkaProperties kafkaProperties) {

		Map<String, Object> props = kafkaProperties.buildConsumerProperties();
		return new DefaultKafkaConsumerFactory<>(
				props,
				new StringDeserializer(),
				new StringDeserializer()
		);
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, String> dltKafkaListenerContainerFactory(
			ConsumerFactory<String, String> consumerFactory
	) {

		ConcurrentKafkaListenerContainerFactory<String, String> factory =
				new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory);
		return factory;
	}

	@Bean
	public ConsumerFactory<String, RentalReturnedEvent> rentalReturnedConsumerFactory(
			KafkaProperties kafkaProperties
	) {

		Map<String, Object> props = kafkaProperties.buildConsumerProperties();
		props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, RentalReturnedEvent.class);
		props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.coc.modi.kafka.event");

		return new DefaultKafkaConsumerFactory<>(
				props,
				new StringDeserializer(),
				new JsonDeserializer<>(RentalReturnedEvent.class),
				false
		);
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, RentalReturnedEvent>
	rentalReturnedKafkaListenerContainerFactory(
			ConsumerFactory<String, RentalReturnedEvent> consumerFactory,
			DefaultErrorHandler errorHandler
	) {

		ConcurrentKafkaListenerContainerFactory<String, RentalReturnedEvent> factory =
				new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory);
		factory.setCommonErrorHandler(errorHandler);
		return factory;
	}

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
			ConsumerFactory<String, SettlementPayoutCompletedEvent> consumerFactory,
			DefaultErrorHandler errorHandler
	) {

		ConcurrentKafkaListenerContainerFactory<String, SettlementPayoutCompletedEvent> factory =
				new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory);
		factory.setCommonErrorHandler(errorHandler);
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
			ConsumerFactory<String, SettlementPayoutFailedEvent> consumerFactory,
			DefaultErrorHandler errorHandler
	) {

		ConcurrentKafkaListenerContainerFactory<String, SettlementPayoutFailedEvent> factory =
				new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory);
		factory.setCommonErrorHandler(errorHandler);
		return factory;
	}
}
