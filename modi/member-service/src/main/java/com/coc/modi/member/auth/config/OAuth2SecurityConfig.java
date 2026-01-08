package com.coc.modi.member.auth.config;

import com.coc.modi.member.auth.oauth2.CustomOAuth2UserService;
import com.coc.modi.member.auth.oauth2.OAuth2LoginFailureHandler;
import com.coc.modi.member.auth.oauth2.OAuth2LoginSuccessHandler;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@ConditionalOnProperty(prefix = "auth.oauth2", name = "enabled", havingValue = "true")
public class OAuth2SecurityConfig {

	@Bean
	@Order(1)
	public SecurityFilterChain oauth2SecurityFilterChain(
			HttpSecurity http,
			CustomOAuth2UserService customOAuth2UserService,
			OAuth2LoginSuccessHandler successHandler,
			OAuth2LoginFailureHandler failureHandler) throws Exception {

		http
				.securityMatcher("/oauth2/**", "/login/oauth2/**")
				.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
				.oauth2Login(oauth2 -> oauth2
						.userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
						.successHandler(successHandler)
						.failureHandler(failureHandler)
				)
				.csrf(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable);

		return http.build();
	}
}
