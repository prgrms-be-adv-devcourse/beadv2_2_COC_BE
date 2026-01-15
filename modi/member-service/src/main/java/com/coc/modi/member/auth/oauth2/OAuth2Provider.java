package com.coc.modi.member.auth.oauth2;

import java.util.Locale;

public enum OAuth2Provider {
	GOOGLE("google"),
	KAKAO("kakao"),
	NAVER("naver");

	private final String registrationId;

	OAuth2Provider(String registrationId) {

		this.registrationId = registrationId;
	}

	public String registrationId() {

		return registrationId;
	}

	public static OAuth2Provider fromRegistrationId(String registrationId) {

		for (OAuth2Provider provider : values()) {
			if (provider.registrationId.equalsIgnoreCase(registrationId)) {
				return provider;
			}
		}
		throw new IllegalArgumentException("Unsupported OAuth2 provider: " + registrationId);
	}

	public String normalized() {

		return registrationId.toLowerCase(Locale.ROOT);
	}
}
