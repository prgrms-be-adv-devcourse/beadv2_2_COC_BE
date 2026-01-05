package com.coc.modi.seller.chat.infrastructure;

import com.coc.modi.seller.chat.application.ChatMessageService;
import com.coc.modi.seller.chat.application.dto.ChatMessageEvent;
import com.coc.modi.seller.chat.application.dto.ChatMessageResponse;
import com.coc.modi.seller.chat.config.ChatSubscriptionTracker;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageSubscriber implements MessageListener {

	private final ObjectMapper objectMapper;
	private final SimpMessagingTemplate messagingTemplate;
	private final ChatMessageService chatMessageService;
	private final ChatSubscriptionTracker chatSubscriptionTracker;

	@Override
	public void onMessage(Message message, byte[] pattern) {
		if (message == null || message.getBody() == null) {
			return;
		}
		ChatMessageEvent event = deserialize(message.getBody());
		if (event == null) {
			return;
		}
		messagingTemplate.convertAndSend("/topic/chat/rooms/" + event.roomId(), ChatMessageResponse.fromEvent(event));
		chatMessageService.markReadForMembers(event.roomId(),
				chatSubscriptionTracker.getActiveMembers(event.roomId()),
				event.messageId());
	}

	private ChatMessageEvent deserialize(byte[] payload) {
		try {
			return objectMapper.readValue(payload, ChatMessageEvent.class);
		} catch (IOException ex) {
			log.warn("Failed to deserialize chat message event", ex);
			return null;
		}
	}
}
