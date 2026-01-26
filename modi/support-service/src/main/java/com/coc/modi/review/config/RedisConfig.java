package com.coc.modi.review.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.coc.modi.review.cache.ReturnedRentalItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class RedisConfig {

	@Bean
	public ObjectMapper redisObjectMapper() {

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		return objectMapper;
	}

	@Bean
	public RedisTemplate<String, ReturnedRentalItem> returnedRentalRedisTemplate(
			RedisConnectionFactory connectionFactory,
			ObjectMapper redisObjectMapper
	) {
		RedisTemplate<String, ReturnedRentalItem> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);

		StringRedisSerializer stringSerializer = new StringRedisSerializer();
		Jackson2JsonRedisSerializer<ReturnedRentalItem> jsonSerializer =
				new Jackson2JsonRedisSerializer<>(ReturnedRentalItem.class);
		jsonSerializer.setObjectMapper(redisObjectMapper);

		template.setKeySerializer(stringSerializer);
		template.setValueSerializer(jsonSerializer);
		template.setHashKeySerializer(stringSerializer);
		template.setHashValueSerializer(jsonSerializer);
		template.afterPropertiesSet();

		return template;
	}
}
