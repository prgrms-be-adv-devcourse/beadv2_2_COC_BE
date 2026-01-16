package com.coc.modi.common.auth;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class InternalApiAuthenticationFilter extends OncePerRequestFilter {
	
	private final String token;
	private final String headerName;
	
	public InternalApiAuthenticationFilter(String token, String headerName) {
		
		this.token = token;
		this.headerName = headerName;
	}
	
	@Override
	protected void doFilterInternal(HttpServletRequest request,
									HttpServletResponse response,
									FilterChain filterChain) throws ServletException, IOException {
		
		String path = request.getServletPath();
		if (!isInternalPath(path)) {
			filterChain.doFilter(request, response);
			return;
		}
		
		if (!StringUtils.hasText(token)) {
			response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
					"Internal API token is not configured");
			return;
		}
		
		String headerValue = request.getHeader(headerName);
		if (!StringUtils.hasText(headerValue) || !headerValue.equals(token)) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
			return;
		}
		
		UsernamePasswordAuthenticationToken authentication =
				new UsernamePasswordAuthenticationToken(
						"internal",
						null,
						List.of(new SimpleGrantedAuthority("ROLE_INTERNAL"))
				);
		
		SecurityContextHolder.getContext().setAuthentication(authentication);
		filterChain.doFilter(request, response);
	}
	
	private boolean isInternalPath(String path) {
		
		return "/internal".equals(path) || path.startsWith("/internal/");
	}
}
