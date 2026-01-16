package com.coc.modi.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "openai.embeddings")
public record AiEmbeddingProperties(
		String model,
		Integer dimensions
) {
	public Integer resolvedDimensions() {
		return dimensions != null && dimensions > 0 ? dimensions : null;
	}
}
