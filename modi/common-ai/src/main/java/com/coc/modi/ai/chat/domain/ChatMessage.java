package com.coc.modi.ai.chat.domain;

public record ChatMessage(String value) {

	public ChatMessage {
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalArgumentException("Message must not be blank");
		}
		value = value.trim();
	}
}
