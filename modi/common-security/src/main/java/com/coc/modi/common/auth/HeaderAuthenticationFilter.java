package com.coc.modi.common.auth;

import java.io.IOException;
import java.util.Arrays;
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

public class HeaderAuthenticationFilter extends OncePerRequestFilter {
	
	private static final String HEADER_MEMBER_ID = "X-Member-Id";
	private static final String HEADER_ROLES = "X-Roles";
	
	@Override
	protected void doFilterInternal(HttpServletRequest request,
									HttpServletResponse response,
									FilterChain filterChain) throws ServletException, IOException {
		
		if (SecurityContextHolder.getContext().getAuthentication() != null) {
			
			filterChain.doFilter(request, response);
			return;
		}
		
		String memberIdHeader = request.getHeader(HEADER_MEMBER_ID);
		String rolesHeader = request.getHeader(HEADER_ROLES);
		
		if (!StringUtils.hasText(memberIdHeader) || !StringUtils.hasText(rolesHeader)) {
			
			filterChain.doFilter(request, response);
			return;
		}
		
		final Long memberId;
		
		try {
			
			memberId = Long.valueOf(memberIdHeader.trim());
		} catch (NumberFormatException e) {
			
			SecurityContextHolder.clearContext();
			filterChain.doFilter(request, response);
			return;
		}
		
		List<String> roles = Arrays.stream(rolesHeader.split(","))
				.map(String::trim)
				.filter(StringUtils::hasText)
				.toList();
		
		String role = resolvePrimaryRole(roles);
		
		List<SimpleGrantedAuthority> authorities = roles.stream()
				.map(r -> new SimpleGrantedAuthority("ROLE_" + r.toUpperCase()))
				.toList();
		
		CustomMember member = new CustomMember(memberId, role);
		
		UsernamePasswordAuthenticationToken authentication =
				new UsernamePasswordAuthenticationToken(
						member,
						null,
						authorities
				);
		
		SecurityContextHolder.getContext().setAuthentication(authentication);
		filterChain.doFilter(request, response);
	}
	
	private String resolvePrimaryRole(List<String> roles) {
		
		if (roles.stream().anyMatch(r -> r.equalsIgnoreCase("ADMIN"))) {
			return "ADMIN";
		}
		if (roles.stream().anyMatch(r -> r.equalsIgnoreCase("SELLER"))) {
			return "SELLER";
		}
		return "MEMBER";
	}
}
