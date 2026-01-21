package com.coc.gateway.security.authz;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "gateway.authz")
public class AuthzCacheProperties {

	private Duration cacheTtl = Duration.ofMinutes(2);
	private Duration staleWindow = Duration.ofMinutes(3);
	private Duration fetchTimeout = Duration.ofSeconds(2);
	private List<String> staleAllowedMethods = new ArrayList<>(List.of("GET"));
	private List<String> freshOnlyPaths = new ArrayList<>(List.of(
			"/seller-service/api/sellers/products/{productId}",
			"/seller-service/api/sellers/self",
			"/seller-service/api/sellers/self/rentals",
			"/seller-service/api/settlements/sellers/self",
			"/seller-service/api/settlements/sellers/self/{sellerSettlementId}",
			"/seller-service/api/settlements/sellers/self/{sellerSettlementId}/lines"
	));

	public Duration getCacheTtl() {
		return cacheTtl;
	}

	public void setCacheTtl(Duration cacheTtl) {
		this.cacheTtl = cacheTtl;
	}

	public Duration getStaleWindow() {
		return staleWindow;
	}

	public void setStaleWindow(Duration staleWindow) {
		this.staleWindow = staleWindow;
	}

	public Duration getFetchTimeout() {
		return fetchTimeout;
	}

	public void setFetchTimeout(Duration fetchTimeout) {
		this.fetchTimeout = fetchTimeout;
	}

	public List<String> getStaleAllowedMethods() {
		return staleAllowedMethods;
	}

	public void setStaleAllowedMethods(List<String> staleAllowedMethods) {
		this.staleAllowedMethods = staleAllowedMethods;
	}

	public List<String> getFreshOnlyPaths() {
		return freshOnlyPaths;
	}

	public void setFreshOnlyPaths(List<String> freshOnlyPaths) {
		this.freshOnlyPaths = freshOnlyPaths;
	}
}
