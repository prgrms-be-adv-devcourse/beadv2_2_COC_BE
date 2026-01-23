package com.coc.modi.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "moderation.ai")
public record ModerationProperties(
		String systemPrompt,
		Double reviewThreshold,
		Double blockThreshold,
		Integer maxTextLength,
		Integer maxSpecValueLength,
		Integer maxImageUrls
) {
	public double resolvedReviewThreshold() {
		return reviewThreshold != null ? reviewThreshold : 0.4d;
	}

	public double resolvedBlockThreshold() {
		return blockThreshold != null ? blockThreshold : 0.7d;
	}

	public int resolvedMaxTextLength() {
		return maxTextLength != null ? maxTextLength : 2000;
	}

	public int resolvedMaxSpecValueLength() {
		return maxSpecValueLength != null ? maxSpecValueLength : 200;
	}

	public int resolvedMaxImageUrls() {
		return maxImageUrls != null ? maxImageUrls : 10;
	}
}
