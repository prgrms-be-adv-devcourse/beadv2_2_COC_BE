package com.coc.modi.review.infrastructure.openai;

import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class OpenAiClient {

	private final RestTemplate restTemplate;
	private final OpenAiProperties properties;

	public OpenAiClient(RestTemplate restTemplate, OpenAiProperties properties) {

		this.restTemplate = restTemplate;
		this.properties = properties;
	}

	public String summarize(String content) {

		OpenAiChatRequest request = new OpenAiChatRequest(
				properties.getModel(),
				List.of(
						new OpenAiChatRequest.Message(
								"system",
								"Summarize the review in 1-2 sentences from the reviewer's perspective. Use first-person voice. Keep it concise and neutral."
						),
						new OpenAiChatRequest.Message("user", content)
				),
				0.2,
				100
		);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(properties.getApiKey());

		HttpEntity<OpenAiChatRequest> entity = new HttpEntity<>(request, headers);

		OpenAiChatResponse response = restTemplate.postForObject(
				properties.getBaseUrl() + "/chat/completions",
				entity,
				OpenAiChatResponse.class
		);

		if (response == null || response.choices() == null || response.choices().isEmpty()) {

			return null;
		}

		OpenAiChatResponse.Choice choice = response.choices().get(0);
		if (choice == null || choice.message() == null) {

			return null;
		}

		return choice.message().content();
	}
}
