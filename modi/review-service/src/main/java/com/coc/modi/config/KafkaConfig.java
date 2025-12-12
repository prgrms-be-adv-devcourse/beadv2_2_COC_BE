package com.coc.modi.config;

import com.coc.modi.common.ReviewCreatedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@EnableKafka
@Configuration
public class KafkaConfig {
	
	@Bean
	public KafkaTemplate<String, ReviewCreatedEvent> reviewKafkaTemplate(
			ProducerFactory<String, ReviewCreatedEvent> producerFactory) {
		
		return new KafkaTemplate<>(producerFactory);
	}
}
