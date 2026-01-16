package com.coc.modi.review.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.coc.modi.review.cache.ReturnedRentalItem;

@Configuration
public class RedisConfig {

	@Bean
	public RedisTemplate<String, ReturnedRentalItem> returnedRentalRedisTemplate(
			RedisConnectionFactory connectionFactory
	) {
		RedisTemplate<String, ReturnedRentalItem> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);

		StringRedisSerializer stringSerializer = new StringRedisSerializer();
		Jackson2JsonRedisSerializer<ReturnedRentalItem> jsonSerializer =
				new Jackson2JsonRedisSerializer<>(ReturnedRentalItem.class);

		template.setKeySerializer(stringSerializer);
		template.setValueSerializer(jsonSerializer);
		template.setHashKeySerializer(stringSerializer);
		template.setHashValueSerializer(jsonSerializer);
		template.afterPropertiesSet();

		return template;
	}
}
