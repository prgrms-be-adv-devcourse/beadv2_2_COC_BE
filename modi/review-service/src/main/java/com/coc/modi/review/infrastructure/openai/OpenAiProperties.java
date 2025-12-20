package com.coc.modi.review.infrastructure.openai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "openai")
public class OpenAiProperties {

	private String apiKey;
	private String baseUrl = "https://api.openai.com/v1";
	private String model = "chatgpt-5-nano";
	private final Summary summary = new Summary();

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getModel() {
		return model;
	}
 
	public void setModel(String model) {
		this.model = model;
	}

	public Summary getSummary() {
		return summary;
	}

	public static class Summary {
		private int maxLength = 200;

		public int getMaxLength() {
			return maxLength;
		}

		public void setMaxLength(int maxLength) {
			this.maxLength = maxLength;
		}
	}
}
