package com.coc.modi.ai.chat.infrastructure;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.coc.modi.ai.config.AiChatProperties;
import com.coc.modi.ai.chat.domain.ChatMessage;
import com.coc.modi.ai.chat.domain.ChatModel;
import com.coc.modi.ai.chat.domain.ChatResult;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
@Component("customOpenAiChatModel")
public class OpenAiChatModel implements ChatModel {

	private final RestClient restClient;
	private final String apiKey;
	private final String model;
	private final Double temperature;
	private final String systemPrompt;
	private final Integer maxTokens;

	public OpenAiChatModel(
			@Value("${openai.api-key:}") String apiKey,
			@Value("${openai.base-url:https://api.openai.com/}") String baseUrl,
			AiChatProperties chatProperties) {

		this.apiKey = apiKey;
		this.model = chatProperties.options() != null ? chatProperties.options().model() : null;
		this.temperature = chatProperties.options() != null ? chatProperties.options().temperature() : null;
		this.maxTokens = chatProperties.options() != null ? chatProperties.options().maxTokens() : null;
		this.systemPrompt = buildSystemPrompt(chatProperties);
		this.restClient = RestClient.builder()
				.baseUrl(trimTrailingSlash(baseUrl))
				.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.build();
	}

	@Override
	public ChatResult chat(ChatMessage message) {
		if (apiKey == null || apiKey.isBlank() || "changeme".equals(apiKey)) {
			return new ChatResult("OpenAI API key not configured", Map.of("source", "fallback"));
		}

		if (isGpt5Model(model)) {
			return chatWithResponsesApi(message);
		}

		List<ChatMessageRequest> messages = systemPrompt == null || systemPrompt.isBlank()
				? List.of(new ChatMessageRequest("user", message.value()))
				: List.of(
						new ChatMessageRequest("system", systemPrompt),
						new ChatMessageRequest("user", message.value())
				);

		ChatRequest request = new ChatRequest(model, temperature, maxTokens, messages);

		JsonNode response;
		try {
			response = restClient.post()
					.uri("/v1/chat/completions")
					.body(request)
					.retrieve()
					.body(JsonNode.class);
		} catch (RestClientException ex) {
			log.warn("OpenAI chat request failed.", ex);
			return new ChatResult("", Map.of("source", "openai", "error", "request_failed"));
		}

		if (response != null) {
			log.info("OpenAI chat raw response: {}", response);
		}

		if (response == null || response.path("choices").isMissingNode() || response.path("choices").isEmpty()) {
			log.warn("OpenAI chat response is empty.");
			return new ChatResult("", Map.of("source", "openai"));
		}

		String content = extractContent(response);
		if ((content == null || content.isBlank()) && isLengthFinish(response)) {
			int retryTokens = Math.max(maxTokens * 2, 512);
			ChatRequest retryRequest = new ChatRequest(model, temperature, retryTokens, messages);
			JsonNode retryResponse;
			try {
				retryResponse = restClient.post()
						.uri("/v1/chat/completions")
						.body(retryRequest)
						.retrieve()
						.body(JsonNode.class);
			} catch (RestClientException ex) {
				log.warn("OpenAI chat retry request failed.", ex);
				retryResponse = null;
			}

			if (retryResponse != null) {
				log.info("OpenAI chat retry response: {}", retryResponse);
			}
			if (retryResponse != null && !retryResponse.path("choices").isMissingNode()
					&& !retryResponse.path("choices").isEmpty()) {
				content = extractContent(retryResponse);
			}
		}
		return new ChatResult(content == null ? "" : content, Map.of("source", "openai"));
	}

	@Override
	public Flux<String> stream(ChatMessage message) {
		ChatResult result = chat(message);
		return Flux.just(result.content());
	}

	public record ChatRequest(String model,
							  Double temperature,
							  @JsonProperty("max_completion_tokens") Integer maxTokens,
							  List<ChatMessageRequest> messages) {
	}

	public record ChatMessageRequest(String role, String content) {
	}

	private ChatResult chatWithResponsesApi(ChatMessage message) {
		List<ResponsesInputMessage> input = systemPrompt == null || systemPrompt.isBlank()
				? List.of(new ResponsesInputMessage("user", List.of(new ResponsesInputText(message.value()))))
				: List.of(
						new ResponsesInputMessage("system", List.of(new ResponsesInputText(systemPrompt))),
						new ResponsesInputMessage("user", List.of(new ResponsesInputText(message.value())))
				);

		int outputTokens = Math.max(maxTokens, 200);
		ResponsesRequest request = new ResponsesRequest(
				model,
				input,
				outputTokens,
				new ResponsesReasoning("minimal"),
				new ResponsesText(new ResponsesTextFormat("text"))
		);

		JsonNode response = callResponses(request);
		String content = response == null ? "" : extractResponsesContent(response);

		if ((content == null || content.isBlank()) && isResponsesTokenLimited(response)) {
			int retryTokens = Math.max(outputTokens * 2, 400);
			ResponsesRequest retryRequest = new ResponsesRequest(
					model,
					input,
					retryTokens,
					new ResponsesReasoning("minimal"),
					new ResponsesText(new ResponsesTextFormat("text"))
			);
			JsonNode retryResponse = callResponses(retryRequest);
			content = retryResponse == null ? "" : extractResponsesContent(retryResponse);
		}

		return new ChatResult(content == null ? "" : content, Map.of("source", "openai"));
	}

	public record ResponsesRequest(
			String model,
			Object input,
			@JsonProperty("max_output_tokens") Integer maxOutputTokens,
			ResponsesReasoning reasoning,
			ResponsesText text
	) {
	}

	public record ResponsesReasoning(String effort) {
	}

	public record ResponsesText(ResponsesTextFormat format) {
	}

	public record ResponsesTextFormat(String type) {
	}

	public record ResponsesInputMessage(String role, List<ResponsesInputText> content) {
	}

	public record ResponsesInputText(String type, String text) {
		public ResponsesInputText(String text) {
			this("input_text", text);
		}
	}

	private static String extractContent(JsonNode response) {
		JsonNode choices = response.path("choices");
		if (!choices.isArray() || choices.isEmpty()) {
			return "";
		}

		JsonNode message = choices.get(0).path("message");
		JsonNode contentNode = message.path("content");
		if (contentNode.isTextual()) {
			return contentNode.asText();
		}

		JsonNode textNode = message.path("text");
		if (textNode.isTextual()) {
			return textNode.asText();
		}

		if (contentNode.isArray()) {
			StringBuilder sb = new StringBuilder();
			for (JsonNode item : contentNode) {
				JsonNode text = item.path("text");
				if (text.isTextual()) {
					if (!sb.isEmpty()) {
						sb.append(' ');
					}
					sb.append(text.asText());
				}
			}
			return sb.toString().trim();
		}

		return "";
	}

	private static String extractResponsesContent(JsonNode response) {
		JsonNode outputText = response.path("output_text");
		if (outputText.isTextual()) {
			return outputText.asText();
		}

		JsonNode output = response.path("output");
		if (output.isArray()) {
			StringBuilder sb = new StringBuilder();
			for (JsonNode item : output) {
				JsonNode content = item.path("content");
				if (!content.isArray()) {
					continue;
				}
				for (JsonNode part : content) {
					JsonNode text = part.path("text");
					if (text.isTextual()) {
						if (!sb.isEmpty()) {
							sb.append(' ');
						}
						sb.append(text.asText());
					}
				}
			}
			return sb.toString().trim();
		}

		return "";
	}

	private JsonNode callResponses(ResponsesRequest request) {
		JsonNode response;
		try {
			response = restClient.post()
					.uri("/v1/responses")
					.body(request)
					.retrieve()
					.body(JsonNode.class);
		} catch (RestClientException ex) {
			log.warn("OpenAI responses request failed.", ex);
			return null;
		}

		if (response != null) {
			log.info("OpenAI responses raw response: {}", response);
		} else {
			log.warn("OpenAI responses is empty.");
		}

		return response;
	}

	private static boolean isResponsesTokenLimited(JsonNode response) {
		if (response == null) {
			return false;
		}
		JsonNode reason = response.path("incomplete_details").path("reason");
		return reason.isTextual() && "max_output_tokens".equalsIgnoreCase(reason.asText());
	}

	private static boolean isGpt5Model(String model) {
		return model != null && model.startsWith("gpt-5");
	}

	private static boolean isLengthFinish(JsonNode response) {
		JsonNode finish = response.path("choices").path(0).path("finish_reason");
		return finish.isTextual() && "length".equalsIgnoreCase(finish.asText());
	}

	private static String buildSystemPrompt(AiChatProperties chatProperties) {
		if (chatProperties == null || chatProperties.template() == null) {
			return defaultSystemPrompt();
		}

		String system = chatProperties.template().system();
		String append = chatProperties.template().append();

		if (append == null || append.isBlank()) {
			return combineWithDefault(system);
		}

		if (system == null || system.isBlank()) {
			return combineWithDefault(append);
		}

		return combineWithDefault(system + "\n" + append);
	}

	private static String combineWithDefault(String base) {
		if (base == null || base.isBlank()) {
			return defaultSystemPrompt();
		}
		return base.trim() + "\n" + defaultSystemPrompt();
	}

	private static String defaultSystemPrompt() {
		return """
				답변은 2~3문장 이내로, 핵심만 요약해.
				불필요한 서론/부연 설명은 생략해.
				""".trim();
	}

	private static String trimTrailingSlash(String value) {
		if (value == null || value.isBlank()) {
			return "https://api.openai.com";
		}
		return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
	}
}
