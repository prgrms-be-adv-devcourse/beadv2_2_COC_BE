package com.coc.modi.review.infrastructure.openai;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OpenAiChatRequest(
		String model,
		List<Message> messages,
		double temperature,
		@JsonProperty("max_tokens") Integer maxTokens
) {
	public record Message(String role, String content) {
	}
}
