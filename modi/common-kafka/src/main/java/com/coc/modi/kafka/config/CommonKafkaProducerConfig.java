package com.coc.modi.kafka.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import org.apache.kafka.common.serialization.StringSerializer;

@Configuration
@ConditionalOnClass(KafkaTemplate.class)
public class CommonKafkaProducerConfig {
	
	@Bean
	@ConditionalOnMissingBean
	public ProducerFactory<String, Object> producerFactory(KafkaProperties kafkaProperties) {
		
		Map<String, Object> config = new HashMap<>(kafkaProperties.buildProducerProperties());
		config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
		
		return new DefaultKafkaProducerFactory<>(config);
	}
	
	@Bean
	@ConditionalOnMissingBean
	public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
		
		return new KafkaTemplate<>(producerFactory);
	}
}
