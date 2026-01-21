package com.coc.modi.member.auth.oauth2;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

class OAuth2MemberInfoTest {

	@Test
	void maps_google_attributes() {

		Map<String, Object> attributes = Map.of(
				"sub", "google-123",
				"email", "user@example.com",
				"name", "Google User"
		);

		OAuth2MemberInfo info = OAuth2MemberInfo.from(OAuth2Provider.GOOGLE, attributes);

		assertThat(info.provider()).isEqualTo(OAuth2Provider.GOOGLE);
		assertThat(info.providerId()).isEqualTo("google-123");
		assertThat(info.email()).isEqualTo("user@example.com");
		assertThat(info.name()).isEqualTo("Google User");
	}

	@Test
	void maps_kakao_attributes_with_profile() {

		Map<String, Object> profile = Map.of(
				"nickname", "Kakao User"
		);
		Map<String, Object> account = Map.of(
				"profile", profile
		);
		Map<String, Object> attributes = Map.of(
				"id", "kakao-456",
				"kakao_account", account
		);

		OAuth2MemberInfo info = OAuth2MemberInfo.from(OAuth2Provider.KAKAO, attributes);

		assertThat(info.provider()).isEqualTo(OAuth2Provider.KAKAO);
		assertThat(info.providerId()).isEqualTo("kakao-456");
		assertThat(info.name()).isEqualTo("Kakao User");
		assertThat(info.email()).isNull();
	}

	@Test
	void maps_naver_attributes_from_response() {

		Map<String, Object> response = Map.of(
				"id", "naver-789",
				"name", "Naver User",
				"email", "naver@example.com"
		);
		Map<String, Object> attributes = Map.of("response", response);

		OAuth2MemberInfo info = OAuth2MemberInfo.from(OAuth2Provider.NAVER, attributes);

		assertThat(info.provider()).isEqualTo(OAuth2Provider.NAVER);
		assertThat(info.providerId()).isEqualTo("naver-789");
		assertThat(info.name()).isEqualTo("Naver User");
		assertThat(info.email()).isEqualTo("naver@example.com");
	}
}
