package com.coc.modi.admin.infrastructure.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.Client;
import feign.httpclient.ApacheHttpClient;

@Configuration
public class AdminFeignClientConfig {

	@Bean
	public Client feignClient() {
		return new ApacheHttpClient();
	}
}
