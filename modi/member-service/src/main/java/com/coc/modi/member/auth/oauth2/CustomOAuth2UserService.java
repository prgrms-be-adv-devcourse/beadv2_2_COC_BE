package com.coc.modi.member.auth.oauth2;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
	
	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) {
		
		OAuth2User oauth2User = super.loadUser(userRequest);
		OAuth2Provider provider = OAuth2Provider.fromRegistrationId(
				userRequest.getClientRegistration().getRegistrationId()
		);
		OAuth2MemberInfo memberInfo = OAuth2MemberInfo.from(provider, oauth2User.getAttributes());
		
		return new CustomOAuth2User(
				memberInfo,
				oauth2User.getAttributes(),
				List.of(new SimpleGrantedAuthority("ROLE_OAUTH2"))
		);
	}
}
