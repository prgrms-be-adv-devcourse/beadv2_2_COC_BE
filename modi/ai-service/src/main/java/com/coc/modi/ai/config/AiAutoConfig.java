package com.coc.modi.ai.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
	@EnableConfigurationProperties({
			AiChatProperties.class,
			AiEmbeddingProperties.class,
			ModerationProperties.class
	})
public class AiAutoConfig {
}
