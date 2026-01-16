package com.coc.gateway.security.authz;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
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
	private final ReactiveStringRedisTemplate redisTemplate;
	private final ObjectMapper objectMapper;
	private final ConcurrentHashMap<Long, AtomicBoolean> refreshInProgress = new ConcurrentHashMap<>();

	public MemberAuthzService(MemberAuthzClient client,
			AuthzCacheProperties properties,
			EndpointPermissionMatcher matcher,
			ReactiveStringRedisTemplate redisTemplate,
			ObjectMapper objectMapper) {
		this.client = client;
		this.properties = properties;
		this.matcher = matcher;
		this.redisTemplate = redisTemplate;
		this.objectMapper = objectMapper;
	}

	public Mono<Set<String>> resolveRoles(Long memberId, String method, String path) {
		boolean allowStale = isStaleAllowed(method, path);
		return Mono.defer(() -> resolveRoles(memberId, allowStale));
	}

	private Mono<Set<String>> resolveRoles(Long memberId, boolean allowStale) {
		String key = cacheKey(memberId);
		return redisTemplate.opsForValue()
				.get(key)
				.flatMap(value -> readEntry(value, memberId, allowStale))
				.switchIfEmpty(fetchAndCache(memberId));
	}

	private Mono<Set<String>> fetchAndCache(Long memberId) {
		return client.fetchMemberAuthz(memberId)
				.map(response -> normalizeRoles(response.roles()))
				.filter(roles -> !roles.isEmpty())
				.flatMap(roles -> writeEntry(memberId, roles).thenReturn(roles))
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

	private Mono<Void> writeEntry(Long memberId, Set<String> roles) {
		Instant now = Instant.now();
		Instant freshUntil = now.plus(properties.getCacheTtl());
		Duration ttl = properties.getCacheTtl().plus(properties.getStaleWindow());
		AuthzCacheEntry entry = new AuthzCacheEntry(List.copyOf(roles), freshUntil.toEpochMilli());

		try {
			String payload = objectMapper.writeValueAsString(entry);
			return redisTemplate.opsForValue().set(cacheKey(memberId), payload, ttl).then();
		} catch (JsonProcessingException ex) {
			log.warn("failed to serialize authz cache entry: memberId={}", memberId, ex);
			return Mono.empty();
		}
	}

	private Mono<Set<String>> readEntry(String payload, Long memberId, boolean allowStale) {
		if (!StringUtils.hasText(payload)) {
			return Mono.empty();
		}

		AuthzCacheEntry entry;
		try {
			entry = objectMapper.readValue(payload, AuthzCacheEntry.class);
		} catch (Exception ex) {
			log.warn("failed to parse authz cache entry: memberId={}", memberId, ex);
			return redisTemplate.delete(cacheKey(memberId)).then(Mono.empty());
		}

		Set<String> roles = normalizeRoles(entry.roles());
		if (roles.isEmpty()) {
			return Mono.empty();
		}

		Instant now = Instant.now();
		Instant freshUntil = Instant.ofEpochMilli(entry.freshUntilEpochMs());
		Instant staleUntil = freshUntil.plus(properties.getStaleWindow());

		if (now.isBefore(freshUntil)) {
			return Mono.just(roles);
		}

		if (allowStale && now.isBefore(staleUntil)) {
			refreshAsync(memberId);
			return Mono.just(roles);
		}

		return Mono.empty();
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

	private String cacheKey(Long memberId) {
		return "authz:user:" + memberId;
	}

	private record AuthzCacheEntry(List<String> roles, long freshUntilEpochMs) {
	}

	public Mono<Void> evictMember(Long memberId) {
		if (memberId == null) {
			return Mono.empty();
		}
		refreshInProgress.remove(memberId);
		return redisTemplate.delete(cacheKey(memberId)).then();
	}
}
