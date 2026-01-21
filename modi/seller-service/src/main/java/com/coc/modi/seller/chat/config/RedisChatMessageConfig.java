package com.coc.modi.seller.chat.config;

import com.coc.modi.seller.chat.infrastructure.ChatMessageSubscriber;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.PatternTopic;

@Configuration
@RequiredArgsConstructor
public class RedisChatMessageConfig {

	private static final String TOPIC_PATTERN = "chat:room:*";

	private final RedisConnectionFactory redisConnectionFactory;
	private final ChatMessageSubscriber chatMessageSubscriber;

	@Bean
	public RedisMessageListenerContainer redisMessageListenerContainer() {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(redisConnectionFactory);
		container.addMessageListener(chatMessageSubscriber, new PatternTopic(TOPIC_PATTERN));
		return container;
	}
}
