package com.coc.modi.member.auth.application;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class RefreshCookieManager {
	
	private static final String COOKIE_NAME = "refresh_token";
	
	private final String cookiePath;
	private final String cookieDomain;
	private final String sameSite;
	
	public RefreshCookieManager(
			@Value("${auth.refresh-cookie.path:/member-service/api/auth}") String cookiePath,
			@Value("${auth.refresh-cookie.domain:}") String cookieDomain,
			@Value("${auth.refresh-cookie.same-site:Lax}") String sameSite) {
		
		this.cookiePath = cookiePath;
		this.cookieDomain = cookieDomain;
		this.sameSite = sameSite;
	}
	
	public ResponseCookie create(String refreshToken, Duration ttl, boolean secure) {
		
		return baseCookie(refreshToken, secure)
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
		
		return baseCookie("", secure)
				.maxAge(0)
				.build();
	}
	
	private ResponseCookie.ResponseCookieBuilder baseCookie(String value, boolean secure) {
		
		ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(COOKIE_NAME, value)
				.httpOnly(true)
				.secure(secure)
				.sameSite(sameSite)
				.path(cookiePath);
		
		if (cookieDomain != null && !cookieDomain.isBlank()) {
			
			builder.domain(cookieDomain);
		}
		
		return builder;
	}
}
