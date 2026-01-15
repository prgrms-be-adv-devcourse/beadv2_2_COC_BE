package com.coc.modi.member.auth.oauth2;

import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class CustomOAuth2User implements OAuth2User {

	private final OAuth2MemberInfo memberInfo;
	private final Map<String, Object> attributes;
	private final Collection<? extends GrantedAuthority> authorities;

	public CustomOAuth2User(OAuth2MemberInfo memberInfo,
							Map<String, Object> attributes,
							Collection<? extends GrantedAuthority> authorities) {

		this.memberInfo = memberInfo;
		this.attributes = attributes;
		this.authorities = authorities;
	}

	public OAuth2MemberInfo getMemberInfo() {

		return memberInfo;
	}

	@Override
	public Map<String, Object> getAttributes() {

		return attributes;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {

		return authorities;
	}

	@Override
	public String getName() {

		return memberInfo.providerId();
	}
}
