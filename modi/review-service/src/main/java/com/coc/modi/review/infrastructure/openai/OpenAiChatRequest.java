package com.coc.modi.review.infrastructure.openai;

import java.util.List;

public record OpenAiChatRequest(
		String model,
		List<Message> messages,
		double temperature,
		Integer maxTokens
) {
	public record Message(String role, String content) {
	}
}
