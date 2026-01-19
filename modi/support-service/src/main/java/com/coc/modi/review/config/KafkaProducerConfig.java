package com.coc.modi.review.config;

import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.coc.modi.kafka.event.NotificationEvent;
import com.coc.modi.common.ReviewCreatedEvent;

@Configuration
public class KafkaProducerConfig {

	@Bean
	public ProducerFactory<String, NotificationEvent> notificationProducerFactory(KafkaProperties kafkaProperties) {

		Map<String, Object> props = kafkaProperties.buildProducerProperties();
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

		return new DefaultKafkaProducerFactory<>(props);
	}

	@Bean
	public KafkaTemplate<String, NotificationEvent> notificationKafkaTemplate(
			ProducerFactory<String, NotificationEvent> notificationProducerFactory) {

		return new KafkaTemplate<>(notificationProducerFactory);
	}

	@Bean
	public ProducerFactory<String, ReviewCreatedEvent> reviewProducerFactory(KafkaProperties kafkaProperties) {

		Map<String, Object> props = kafkaProperties.buildProducerProperties();
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

		return new DefaultKafkaProducerFactory<>(props);
	}

	@Bean
	public KafkaTemplate<String, ReviewCreatedEvent> reviewKafkaTemplate(
			ProducerFactory<String, ReviewCreatedEvent> reviewProducerFactory) {

		return new KafkaTemplate<>(reviewProducerFactory);
	}
}
