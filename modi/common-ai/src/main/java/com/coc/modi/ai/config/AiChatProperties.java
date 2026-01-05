package com.coc.modi.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "openai.chat")
public record AiChatProperties(
		Template template,
		Options options
) {
	public record Template(String system, String append) {
	}

	public record Options(String model, Double temperature, Integer maxTokens) {
	}
}
