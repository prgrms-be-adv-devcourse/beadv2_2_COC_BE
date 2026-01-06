package com.coc.modi.seller.chat.infrastructure;

import com.coc.modi.seller.chat.application.dto.ChatMessageEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisChatMessagePublisher implements ChatMessagePublisher {

	private final ObjectMapper objectMapper;
	private final StringRedisTemplate stringRedisTemplate;

	@Override
	public void publish(ChatMessageEvent event) {
		if (event == null) {
			return;
		}
		try {
			String payload = objectMapper.writeValueAsString(event);
			stringRedisTemplate.convertAndSend(channel(event.roomId()), payload);
		} catch (JsonProcessingException ex) {
			log.warn("Failed to serialize chat message event", ex);
		}
	}

	private String channel(Long roomId) {
		return "chat:room:" + roomId;
	}
}
