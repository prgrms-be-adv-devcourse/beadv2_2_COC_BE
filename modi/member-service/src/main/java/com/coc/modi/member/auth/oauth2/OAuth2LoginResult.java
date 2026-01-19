package com.coc.modi.member.auth.oauth2;

import com.coc.modi.member.member.domain.Member;

public record OAuth2LoginResult(
		OAuth2LoginStatus status,
		Member member,
		String signupToken
) {

	public static OAuth2LoginResult login(Member member) {

		return new OAuth2LoginResult(OAuth2LoginStatus.LOGIN, member, null);
	}

	public static OAuth2LoginResult signupRequired(String signupToken) {

		return new OAuth2LoginResult(OAuth2LoginStatus.SIGNUP_REQUIRED, null, signupToken);
	}

	public boolean isLogin() {

		return status == OAuth2LoginStatus.LOGIN;
	}
}
