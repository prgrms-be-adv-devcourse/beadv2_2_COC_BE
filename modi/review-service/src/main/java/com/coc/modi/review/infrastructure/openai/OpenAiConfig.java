package com.coc.modi.review.infrastructure.openai;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties(OpenAiProperties.class)
public class OpenAiConfig {

	@Bean
	public RestTemplate openAiRestTemplate(RestTemplateBuilder builder) {

		HttpComponentsClientHttpRequestFactory factory =
				new HttpComponentsClientHttpRequestFactory();

		factory.setConnectTimeout(5_000);  // ms
		factory.setReadTimeout(10_000);    // ms

		return builder
				.requestFactory(() -> factory)
				.build();
	}
}
