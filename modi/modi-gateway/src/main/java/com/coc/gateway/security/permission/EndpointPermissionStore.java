package com.coc.gateway.security.permission;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Component
public class EndpointPermissionStore {

	private static final String QUERY_ALL = """
			SELECT id, method, path_pattern, roles
			FROM gateway_endpoint_permission
		""";

	private final String url;
	private final String username;
	private final String password;
	private final Duration cacheTtl;
	private final AtomicReference<CacheState> cache = new AtomicReference<>(CacheState.empty());
	private final AtomicBoolean refreshInProgress = new AtomicBoolean(false);

	public EndpointPermissionStore(
			@Value("${spring.datasource.url:}") String url,
			@Value("${spring.datasource.username:}") String username,
			@Value("${spring.datasource.password:}") String password,
			@Value("${gateway.permission-cache-ttl:60s}") Duration cacheTtl) {
		this.url = url;
		this.username = username;
		this.password = password;
		this.cacheTtl = cacheTtl;
	}

	public Flux<EndpointPermission> findCandidates(String method) {

		String normalizedMethod = normalizeMethod(method);
		if (!StringUtils.hasText(normalizedMethod)) {
			return Flux.empty();
		}

		return Mono.defer(this::loadPermissions)
				.flatMapMany(permissions -> Flux.fromIterable(filterByMethod(permissions, normalizedMethod)));
	}

	private Mono<List<EndpointPermission>> loadPermissions() {

		CacheState snapshot = cache.get();
		Instant now = Instant.now();
		if (snapshot.isValid(now)) {
			return Mono.just(snapshot.permissions());
		}

		return refreshPermissions(snapshot);
	}

	private Mono<List<EndpointPermission>> refreshPermissions(CacheState snapshot) {

		if (!refreshInProgress.compareAndSet(false, true)) {
			return Mono.just(snapshot.permissions());
		}

		return Mono.fromCallable(this::queryAll)
				.subscribeOn(Schedulers.boundedElastic())
				.doOnNext(permissions -> {
					Instant expiresAt = Instant.now().plus(cacheTtl);
					cache.set(new CacheState(permissions, expiresAt));
					log.debug("permission cache refreshed: count={}, ttl={}s", permissions.size(), cacheTtl.getSeconds());
				})
				.onErrorResume(ex -> {
					log.error("permission cache refresh failed", ex);
					return Mono.just(snapshot.permissions());
				})
				.doFinally(signal -> refreshInProgress.set(false));
	}

	private List<EndpointPermission> queryAll() throws SQLException {

		if (!StringUtils.hasText(url)) {
			throw new IllegalStateException("spring.datasource.url is empty");
		}

		List<EndpointPermission> results = new ArrayList<>();

		try (Connection connection = openConnection();
				PreparedStatement statement = connection.prepareStatement(QUERY_ALL);
				ResultSet resultSet = statement.executeQuery()) {
			while (resultSet.next()) {
				EndpointPermission permission = new EndpointPermission();
				permission.setId(resultSet.getLong("id"));
				permission.setMethod(resultSet.getString("method"));
				permission.setPathPattern(resultSet.getString("path_pattern"));
				permission.setRoles(resultSet.getString("roles"));
				results.add(permission);
			}
		}

		return results;
	}

	private Connection openConnection() throws SQLException {

		if (StringUtils.hasText(username)) {
			return DriverManager.getConnection(url, username, password);
		}
		return DriverManager.getConnection(url);
	}

	private List<EndpointPermission> filterByMethod(List<EndpointPermission> permissions, String method) {

		return permissions.stream()
				.filter(permission -> matchesMethod(permission.getMethod(), method))
				.toList();
	}

	private boolean matchesMethod(String configuredMethod, String method) {

		if (!StringUtils.hasText(configuredMethod)) {
			return true;
		}

		String normalized = normalizeMethod(configuredMethod);
		return "ALL".equals(normalized) || "*".equals(normalized) || normalized.equals(method);
	}

	private String normalizeMethod(String method) {

		if (!StringUtils.hasText(method)) {
			return "";
		}
		return method.trim().toUpperCase(Locale.ROOT);
	}

	private record CacheState(List<EndpointPermission> permissions, Instant expiresAt) {

		static CacheState empty() {
			return new CacheState(List.of(), Instant.EPOCH);
		}

		boolean isValid(Instant now) {
			return now.isBefore(expiresAt);
		}
	}
}
