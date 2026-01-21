package com.coc.modi.review.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "review")
public record ReviewPolicyProperties(
		Duration reviewableWindow
) {
	public ReviewPolicyProperties {
		if (reviewableWindow == null) {
			reviewableWindow = Duration.ofDays(7);
		}
	}
}
