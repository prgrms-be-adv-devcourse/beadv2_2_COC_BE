package com.coc.modi.account.deposit.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {

	@Value("${toss.payments.connect-timeout-ms:2000}")
	private long connectTimeoutMs;

	@Value("${toss.payments.read-timeout-ms:5000}")
	private long readTimeoutMs;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {

        return builder
				.setConnectTimeout(Duration.ofMillis(connectTimeoutMs))
				.setReadTimeout(Duration.ofMillis(readTimeoutMs))
				.build();
    }
}
