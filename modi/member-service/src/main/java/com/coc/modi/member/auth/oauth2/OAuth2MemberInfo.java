package com.coc.modi.member.auth.oauth2;

import java.util.Map;

public record OAuth2MemberInfo(
		OAuth2Provider provider,
		String providerId,
		String email,
		String name
) {

	public static OAuth2MemberInfo from(OAuth2Provider provider, Map<String, Object> attributes) {

		return switch (provider) {
			case GOOGLE -> fromGoogle(provider, attributes);
			case KAKAO -> fromKakao(provider, attributes);
			case NAVER -> fromNaver(provider, attributes);
		};
	}

	private static OAuth2MemberInfo fromGoogle(OAuth2Provider provider, Map<String, Object> attributes) {

		String providerId = getString(attributes, "sub");
		String email = getString(attributes, "email");
		String name = getString(attributes, "name");
		return new OAuth2MemberInfo(provider, providerId, email, name);
	}

	private static OAuth2MemberInfo fromKakao(OAuth2Provider provider, Map<String, Object> attributes) {

		String providerId = getString(attributes, "id");
		String email = null;
		String name = null;

		Map<String, Object> kakaoAccount = getMap(attributes, "kakao_account");
		if (kakaoAccount != null) {
			email = getString(kakaoAccount, "email");
			Map<String, Object> profile = getMap(kakaoAccount, "profile");
			if (profile != null) {
				name = getString(profile, "nickname");
			}
		}
		if (name == null) {
			Map<String, Object> properties = getMap(attributes, "properties");
			if (properties != null) {
				name = getString(properties, "nickname");
			}
		}
		return new OAuth2MemberInfo(provider, providerId, email, name);
	}

	private static OAuth2MemberInfo fromNaver(OAuth2Provider provider, Map<String, Object> attributes) {

		Map<String, Object> response = getMap(attributes, "response");
		String providerId = getString(response, "id");
		String email = getString(response, "email");
		String name = getString(response, "name");
		if (name == null) {
			name = getString(response, "nickname");
		}
		return new OAuth2MemberInfo(provider, providerId, email, name);
	}

	private static Map<String, Object> getMap(Map<String, Object> source, String key) {

		if (source == null) {
			return null;
		}
		Object value = source.get(key);
		if (value instanceof Map<?, ?> mapValue) {
			@SuppressWarnings("unchecked")
			Map<String, Object> casted = (Map<String, Object>) mapValue;
			return casted;
		}
		return null;
	}

	private static String getString(Map<String, Object> source, String key) {

		if (source == null) {
			return null;
		}
		Object value = source.get(key);
		return value == null ? null : value.toString();
	}
}
