package com.coc.modi.member.auth.config;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "auth.oauth2")
public class OAuth2Properties {

	private boolean enabled;
	private String successRedirect;
	private String failureRedirect;
	private String signupRequiredRedirect;
	private long signupTokenTtlMinutes = 10L;
}
