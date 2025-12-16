package com.coc.modi.member.auth.application;

import java.time.Duration;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class RefreshCookieManager {
	
	private static final String COOKIE_NAME = "refresh_token";
	private static final String COOKIE_PATH = "/member-service/api/auth";
	private static final String PATH = "/";
	
	public ResponseCookie create(String refreshToken, Duration ttl, boolean secure) {
		
		return ResponseCookie.from(COOKIE_NAME, refreshToken)
				.httpOnly(true)
				.secure(secure)
				.sameSite("Lax")
				.path(COOKIE_PATH)
				.maxAge(ttl)
				.build();
	}
	
	public String extract(HttpServletRequest request) {
		
		if (request.getCookies() == null) return null;
		
		for (Cookie c : request.getCookies()) {
			
			if (COOKIE_NAME.equals(c.getName())) return c.getValue();
		}
		
		return null;
	}
	
	public ResponseCookie clear(boolean secure) {
		
		return ResponseCookie.from(COOKIE_NAME, "")
				.httpOnly(true)
				.secure(secure)
				.sameSite("Lax")
				.path(PATH)
				.maxAge(0)
				.build();
	}
}
