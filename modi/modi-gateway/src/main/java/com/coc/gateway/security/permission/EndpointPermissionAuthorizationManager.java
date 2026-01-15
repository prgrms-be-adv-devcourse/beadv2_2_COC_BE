package com.coc.gateway.security.permission;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.web.server.ServerWebExchange;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class EndpointPermissionAuthorizationManager implements ReactiveAuthorizationManager<AuthorizationContext> {

	private final EndpointPermissionStore store;
	private final EndpointPermissionMatcher matcher;

	@Override
	public Mono<AuthorizationDecision> check(Mono<Authentication> authentication, AuthorizationContext context) {

		ServerWebExchange exchange = context.getExchange();
		String method = exchange.getRequest().getMethod().name();
		if (!StringUtils.hasText(method)) {
			return Mono.just(new AuthorizationDecision(false));
		}

		String path = exchange.getRequest().getPath().pathWithinApplication().value();
		String normalizedMethod = method.toUpperCase(Locale.ROOT);

		return authentication
				.filter(Authentication::isAuthenticated)
				.flatMap(auth -> {
					Set<String> roles = extractRoles(auth.getAuthorities());
					return store.findCandidates(normalizedMethod)
							.filter(permission -> matcher.matches(permission.getPathPattern(), path))
							.filter(permission -> isRoleAllowed(permission.getRoles(), roles))
							.hasElements()
							.doOnNext(allowed -> {
								if (!allowed) {
									log.warn("permission denied: method={}, path={}, roles={}", normalizedMethod, path, roles);
								}
							})
							.map(AuthorizationDecision::new);
				})
				.switchIfEmpty(Mono.fromSupplier(() -> {
					log.warn("permission denied: unauthenticated request method={}, path={}", normalizedMethod, path);
					return new AuthorizationDecision(false);
				}))
				.onErrorResume(ex -> {
					log.error("permission check error: method={}, path={}", normalizedMethod, path, ex);
					return Mono.just(new AuthorizationDecision(false));
				});
	}

	private Set<String> extractRoles(Collection<? extends GrantedAuthority> authorities) {

		return authorities.stream()
				.map(GrantedAuthority::getAuthority)
				.filter(StringUtils::hasText)
				.map(this::normalizeRole)
				.collect(Collectors.toSet());
	}

	private boolean isRoleAllowed(String roles, Set<String> memberRoles) {

		if (!StringUtils.hasText(roles)) {
			return true;
		}

		return Arrays.stream(roles.split(","))
				.map(String::trim)
				.filter(StringUtils::hasText)
				.map(this::normalizeRole)
				.anyMatch(memberRoles::contains);
	}

	private String normalizeRole(String role) {

		String normalized = role.trim();
		if (normalized.startsWith("ROLE_")) {
			normalized = normalized.substring("ROLE_".length());
		}
		return normalized.toUpperCase(Locale.ROOT);
	}
}
