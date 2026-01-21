package com.coc.gateway.security.authz;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Component
public class MemberAuthzClient {

	private final WebClient webClient;
	private final String internalHeaderName;
	private final String internalToken;
	private final AuthzCacheProperties properties;

	public MemberAuthzClient(
			@Value("${member-service.url:http://member-service:8085}") String baseUrl,
			WebClient.Builder builder,
			AuthzCacheProperties properties,
			@Value("${internal.api.header:X-Internal-Token}") String internalHeaderName,
			@Value("${internal.api.token:}") String internalToken) {
		this.webClient = builder.baseUrl(baseUrl).build();
		this.properties = properties;
		this.internalHeaderName = internalHeaderName;
		this.internalToken = internalToken;
	}

	public Mono<MemberAuthzResponse> fetchMemberAuthz(Long memberId) {
		WebClient.RequestHeadersSpec<?> request = webClient.get()
				.uri("/internal/members/{memberId}/authz", memberId);

		if (StringUtils.hasText(internalToken)) {
			request = request.header(internalHeaderName, internalToken);
		}

		return request.retrieve()
				.onStatus(HttpStatusCode::isError, response -> response.createException().flatMap(Mono::error))
				.bodyToMono(MemberAuthzResponse.class)
				.timeout(properties.getFetchTimeout());
	}
}
