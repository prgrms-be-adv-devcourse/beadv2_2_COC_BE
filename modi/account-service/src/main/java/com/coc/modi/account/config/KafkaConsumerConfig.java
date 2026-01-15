package com.coc.modi.account.config;

import java.util.Map;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import com.coc.modi.kafka.event.SettlementPayoutRequestedEvent;
import com.coc.modi.kafka.event.MemberCreatedEvent;

@Configuration
public class KafkaConsumerConfig {

	@Bean
	public ConsumerFactory<String, SettlementPayoutRequestedEvent> settlementPayoutConsumerFactory(
			KafkaProperties kafkaProperties
	) {

		Map<String, Object> props = kafkaProperties.buildConsumerProperties();
		props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, SettlementPayoutRequestedEvent.class);
		props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.coc.modi.kafka.event");

		return new DefaultKafkaConsumerFactory<>(
				props,
				new StringDeserializer(),
				new JsonDeserializer<>(SettlementPayoutRequestedEvent.class),
				false
		);
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, SettlementPayoutRequestedEvent>
	settlementPayoutKafkaListenerContainerFactory(
			ConsumerFactory<String, SettlementPayoutRequestedEvent> consumerFactory
	) {

		ConcurrentKafkaListenerContainerFactory<String, SettlementPayoutRequestedEvent> factory =
				new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory);

		return factory;
	}
  @Bean
  public ConsumerFactory<String, MemberCreatedEvent> memberCreatedConsumerFactory(KafkaProperties kafkaProperties) {

      Map<String, Object> props = kafkaProperties.buildConsumerProperties();
      props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, MemberCreatedEvent.class);
      props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.coc.modi.kafka.event");

      return new DefaultKafkaConsumerFactory<>(
              props,
              new StringDeserializer(),
              new JsonDeserializer<>(MemberCreatedEvent.class),
              false
      );
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, MemberCreatedEvent> memberCreatedKafkaListenerContainerFactory(
          ConsumerFactory<String, MemberCreatedEvent> consumerFactory) {

      ConcurrentKafkaListenerContainerFactory<String, MemberCreatedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
      factory.setConsumerFactory(consumerFactory);

      return factory;
  }
}
