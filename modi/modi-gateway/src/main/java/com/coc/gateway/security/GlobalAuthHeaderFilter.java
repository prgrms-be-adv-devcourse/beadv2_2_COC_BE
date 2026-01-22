package com.coc.gateway.security;

import java.util.List;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

public class GlobalAuthHeaderFilter implements WebFilter {
	

	
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		ServerWebExchange strippedExchange = stripAuthHeaders(exchange);

		return ReactiveSecurityContextHolder.getContext()
				.map(SecurityContext::getAuthentication)
				.filter(Authentication::isAuthenticated)
				.flatMap(auth -> {
					
					Object principal = auth.getPrincipal();
					
					if (!(principal instanceof Long memberId)) {
						return chain.filter(strippedExchange);
					}
					
					List<String> roles = auth.getAuthorities()
							.stream().map(a -> a.getAuthority().replace("ROLE_", ""))
							.toList();
					
					ServerHttpRequest mutatedRequest = strippedExchange.getRequest()
							.mutate()
							.headers(h -> {
								h.add("X-Member-Id", String.valueOf(memberId));
								h.add("X-Roles", String.join(",", roles));
							})
							.build();
					
					return chain.filter(
							strippedExchange.mutate().request(mutatedRequest).build()
					);
				})
				.switchIfEmpty(chain.filter(strippedExchange));
	}

	private ServerWebExchange stripAuthHeaders(ServerWebExchange exchange) {
		ServerHttpRequest mutatedRequest = exchange.getRequest()
				.mutate()
				.headers(h -> {
					h.remove("X-Member-Id");
					h.remove("X-Roles");
				})
				.build();
		return exchange.mutate().request(mutatedRequest).build();
	}
}
