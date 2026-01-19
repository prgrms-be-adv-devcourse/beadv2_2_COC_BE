package com.coc.modi.member.auth.oauth2;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.coc.modi.member.auth.application.MemberAuthService;
import com.coc.modi.member.auth.application.dto.MemberLoginResponse;
import com.coc.modi.member.auth.config.OAuth2Properties;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

	private final OAuth2AuthService oauth2AuthService;
	private final MemberAuthService memberAuthService;
	private final OAuth2Properties properties;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request,
									HttpServletResponse response,
									Authentication authentication) throws IOException {

		CustomOAuth2User oauth2User = (CustomOAuth2User) authentication.getPrincipal();
		OAuth2LoginResult result = oauth2AuthService.handleLogin(oauth2User.getMemberInfo());

		if (result.isLogin()) {
			MemberLoginResponse loginResponse = memberAuthService.issueTokens(result.member(), request.isSecure());
			response.addHeader(HttpHeaders.SET_COOKIE, loginResponse.refreshCookie().toString());
			response.sendRedirect(properties.getSuccessRedirect());
			return;
		}

		String redirect = withParams(
				properties.getSignupRequiredRedirect(),
				"status", "signup_required",
				"token", result.signupToken()
		);
		response.sendRedirect(redirect);
	}

	private String withParams(String baseUrl, String key1, String value1, String key2, String value2) {

		String encoded1 = URLEncoder.encode(value1, StandardCharsets.UTF_8);
		String encoded2 = URLEncoder.encode(value2, StandardCharsets.UTF_8);
		String delimiter = baseUrl.contains("?") ? "&" : "?";
		return baseUrl + delimiter + key1 + "=" + encoded1 + "&" + key2 + "=" + encoded2;
	}
}
