package com.coc.modi.member.auth.oauth2;

public record OAuth2SignupPayload(
		String provider,
		String providerId,
		String name,
		String email
) {

	public static OAuth2SignupPayload from(OAuth2MemberInfo info) {

		String name = info.name();
		String email = info.email();
		return new OAuth2SignupPayload(
				info.provider().normalized(),
				info.providerId(),
				name,
				email
		);
	}
}
