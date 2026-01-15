package com.coc.modi.ai.chat.application;

import com.coc.modi.ai.chat.domain.ChatMessage;
import com.coc.modi.ai.chat.domain.ChatModel;
import com.coc.modi.ai.chat.domain.ChatResult;
import com.coc.modi.ai.exception.AiInputInvalidException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class ChatService {

	private static final int MAX_MESSAGE_LENGTH = 1000;

	private final ChatModel chatModel;

	public ChatResult chat(String message) {
		return chatModel.chat(new ChatMessage(normalizeMessage(message)));
	}

	public Flux<String> stream(String message) {
		return chatModel.stream(new ChatMessage(normalizeMessage(message)));
	}

	private static String normalizeMessage(String message) {
		if (message == null) {
			throw new AiInputInvalidException("메시지는 필수입니다.");
		}
		String trimmed = message.trim();
		if (trimmed.isEmpty()) {
			throw new AiInputInvalidException("메시지는 필수입니다.");
		}
		if (trimmed.length() > MAX_MESSAGE_LENGTH) {
			throw new AiInputInvalidException("메시지는 1000자 이하여야 합니다.");
		}
		return trimmed;
	}
}
