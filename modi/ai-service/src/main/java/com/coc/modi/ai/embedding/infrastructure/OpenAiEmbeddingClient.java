package com.coc.modi.ai.embedding.infrastructure;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.coc.modi.ai.config.AiEmbeddingProperties;
import com.coc.modi.ai.embedding.domain.EmbeddingClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OpenAiEmbeddingClient implements EmbeddingClient {

	private static final int DEFAULT_EMBEDDING_DIMENSIONS = 1536;
	private final RestClient restClient;
	private final String model;
	private final Integer dimensions;

	public OpenAiEmbeddingClient(
			@Value("${openai.api-key}") String apiKey,
			@Value("${openai.base-url:https://api.openai.com/}") String baseUrl,
			AiEmbeddingProperties embeddingProperties) {

		this.model = embeddingProperties.model() != null ? embeddingProperties.model() : "text-embedding-3-small";
		this.dimensions = embeddingProperties.resolvedDimensions();
		if (this.dimensions != null && this.dimensions != DEFAULT_EMBEDDING_DIMENSIONS) {
			throw new IllegalStateException("Embedding dimensions must be " + DEFAULT_EMBEDDING_DIMENSIONS
					+ " to match product_embedding schema.");
		}
		this.restClient = RestClient.builder()
				.baseUrl(trimTrailingSlash(baseUrl))
				.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.build();
	}

	@Override
	public List<Double> embed(String input) {
		if (input == null || input.isBlank()) {
			return List.of();
		}

		EmbeddingRequest request = new EmbeddingRequest(model, input, dimensions);
		EmbeddingResponse response = restClient.post()
				.uri("/v1/embeddings")
				.body(request)
				.retrieve()
				.body(EmbeddingResponse.class);

		if (response == null || response.data() == null || response.data().isEmpty()) {
			log.warn("OpenAI embedding response is empty.");
			return List.of();
		}

		return response.data().get(0).embedding();
	}

	public record EmbeddingRequest(String model, String input, Integer dimensions) {
	}

	public record EmbeddingResponse(List<EmbeddingData> data) {
	}

	public record EmbeddingData(List<Double> embedding) {
	}

	private static String trimTrailingSlash(String value) {
		if (value == null || value.isBlank()) {
			return "https://api.openai.com";
		}
		return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
	}
}
