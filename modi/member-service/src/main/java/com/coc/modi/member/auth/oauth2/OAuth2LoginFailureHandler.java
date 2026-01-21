package com.coc.modi.member.auth.oauth2;

import java.io.IOException;

import com.coc.modi.member.auth.config.OAuth2Properties;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

	private final OAuth2Properties properties;

	@Override
	public void onAuthenticationFailure(HttpServletRequest request,
									HttpServletResponse response,
									AuthenticationException exception) throws IOException {

		response.sendRedirect(properties.getFailureRedirect());
	}
}
