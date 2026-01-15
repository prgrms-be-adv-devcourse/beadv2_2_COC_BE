package com.coc.gateway.security.authz;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.coc.gateway.security.permission.EndpointPermissionMatcher;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class MemberAuthzService {

	private final MemberAuthzClient client;
	private final AuthzCacheProperties properties;
	private final EndpointPermissionMatcher matcher;
	private final ConcurrentHashMap<Long, CacheEntry> cache = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<Long, AtomicBoolean> refreshInProgress = new ConcurrentHashMap<>();

	public MemberAuthzService(MemberAuthzClient client,
			AuthzCacheProperties properties,
			EndpointPermissionMatcher matcher) {
		this.client = client;
		this.properties = properties;
		this.matcher = matcher;
	}

	public Mono<Set<String>> resolveRoles(Long memberId, String method, String path) {
		boolean allowStale = isStaleAllowed(method, path);
		return Mono.defer(() -> resolveRoles(memberId, allowStale));
	}

	private Mono<Set<String>> resolveRoles(Long memberId, boolean allowStale) {
		CacheEntry entry = cache.get(memberId);
		Instant now = Instant.now();

		if (entry != null && now.isBefore(entry.freshUntil())) {
			return Mono.just(entry.roles());
		}

		if (entry != null && now.isBefore(entry.staleUntil())) {
			if (allowStale) {
				refreshAsync(memberId);
				return Mono.just(entry.roles());
			}
		}

		return fetchAndCache(memberId);
	}

	private Mono<Set<String>> fetchAndCache(Long memberId) {
		return client.fetchMemberAuthz(memberId)
				.map(response -> normalizeRoles(response.roles()))
				.filter(roles -> !roles.isEmpty())
				.doOnNext(roles -> cache.put(memberId, buildEntry(roles)))
				.onErrorResume(ex -> {
					log.warn("member authz fetch failed: memberId={}", memberId, ex);
					return Mono.empty();
				});
	}

	private void refreshAsync(Long memberId) {
		AtomicBoolean flag = refreshInProgress.computeIfAbsent(memberId, id -> new AtomicBoolean(false));
		if (!flag.compareAndSet(false, true)) {
			return;
		}

		fetchAndCache(memberId)
				.doFinally(signal -> flag.set(false))
				.subscribe();
	}

	private CacheEntry buildEntry(Set<String> roles) {
		Instant now = Instant.now();
		Instant freshUntil = now.plus(properties.getCacheTtl());
		Instant staleUntil = freshUntil.plus(properties.getStaleWindow());
		return new CacheEntry(Set.copyOf(roles), freshUntil, staleUntil);
	}

	private Set<String> normalizeRoles(List<String> roles) {
		if (roles == null) {
			return Set.of();
		}
		return roles.stream()
				.filter(StringUtils::hasText)
				.map(this::normalizeRole)
				.collect(Collectors.toSet());
	}

	private String normalizeRole(String role) {
		String normalized = role.trim();
		if (normalized.startsWith("ROLE_")) {
			normalized = normalized.substring("ROLE_".length());
		}
		return normalized.toUpperCase(Locale.ROOT);
	}

	private boolean isStaleAllowed(String method, String path) {
		if (!StringUtils.hasText(method) || !StringUtils.hasText(path)) {
			return false;
		}

		String normalizedMethod = method.trim().toUpperCase(Locale.ROOT);
		boolean methodAllowed = properties.getStaleAllowedMethods().stream()
				.filter(StringUtils::hasText)
				.map(m -> m.trim().toUpperCase(Locale.ROOT))
				.anyMatch(normalizedMethod::equals);

		if (!methodAllowed) {
			return false;
		}

		for (String pattern : properties.getFreshOnlyPaths()) {
			if (StringUtils.hasText(pattern) && matcher.matches(pattern, path)) {
				return false;
			}
		}

		return true;
	}

	private record CacheEntry(Set<String> roles, Instant freshUntil, Instant staleUntil) {
	}

	public void evictMember(Long memberId) {
		if (memberId == null) {
			return;
		}
		cache.remove(memberId);
		refreshInProgress.remove(memberId);
	}
}
