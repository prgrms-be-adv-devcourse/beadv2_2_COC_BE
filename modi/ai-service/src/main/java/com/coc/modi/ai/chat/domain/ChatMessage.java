package com.coc.modi.ai.chat.domain;

import com.coc.modi.ai.exception.AiInputInvalidException;

public record ChatMessage(String value) {

	private static final int MAX_MESSAGE_LENGTH = 1000;

	public ChatMessage {
		if (value == null || value.trim().isEmpty()) {
			throw new AiInputInvalidException("메시지는 필수입니다.");
		}
		String trimmed = value.trim();
		if (trimmed.length() > MAX_MESSAGE_LENGTH) {
			throw new AiInputInvalidException("메시지는 1000자 이하여야 합니다.");
		}
		value = trimmed;
	}
}
