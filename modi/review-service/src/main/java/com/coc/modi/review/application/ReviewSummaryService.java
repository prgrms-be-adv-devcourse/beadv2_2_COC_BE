package com.coc.modi.review.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.coc.modi.review.infrastructure.openai.OpenAiClient;
import com.coc.modi.review.infrastructure.openai.OpenAiProperties;

@Service
public class ReviewSummaryService {

	private static final Logger log = LoggerFactory.getLogger(ReviewSummaryService.class);
	private final OpenAiClient openAiClient;
	private final OpenAiProperties properties;

	public ReviewSummaryService(OpenAiClient openAiClient, OpenAiProperties properties) {

		this.openAiClient = openAiClient;
		this.properties = properties;
	}

	public String summarize(String content) {

		if (content == null || content.isBlank()) {
			return null;
		}

		if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
			return fallback(content);
		}

		try {
			String summary = openAiClient.summarize(content);
			if (summary == null || summary.isBlank()) {
				return fallback(content);
			}

			return normalize(summary);

		} catch (Exception ex) {

			log.warn("Failed to summarize review content with OpenAI", ex);

			return fallback(content);
		}
	}

	private String normalize(String summary) {

		String trimmed = summary.replace("\n", " ").trim();
		int maxLength = properties.getSummary().getMaxLength();
		if (trimmed.length() <= maxLength) {

			return trimmed;
		}

		return trimmed.substring(0, maxLength).trim();
	}

	private String fallback(String content) {

		String trimmed = content.replace("\n", " ").trim();
		int maxLength = properties.getSummary().getMaxLength();
		if (trimmed.length() <= maxLength) {

			return trimmed;
		}

		return trimmed.substring(0, maxLength).trim();
	}
}
