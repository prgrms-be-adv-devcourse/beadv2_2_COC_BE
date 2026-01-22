package com.coc.modi.notification.infrastructure.client.member;

import org.springframework.context.annotation.Bean;

import feign.Client;
import feign.httpclient.ApacheHttpClient;

public class MemberFeignClientConfig {

	@Bean
	public Client feignClient() {
		return new ApacheHttpClient();
	}
}
