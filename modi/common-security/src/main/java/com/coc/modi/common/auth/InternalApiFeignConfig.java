package com.coc.modi.common.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import feign.RequestInterceptor;

@Configuration
public class InternalApiFeignConfig {
	
	@Value("${internal.api.token:}")
	private String token;
	
	@Value("${internal.api.header:X-Internal-Token}")
	private String headerName;
	
	@Bean
	public RequestInterceptor internalApiRequestInterceptor() {
		
		return template -> {
			if (StringUtils.hasText(token)) {
				template.header(headerName, token);
			}
		};
	}
}
