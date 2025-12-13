package com.coc.gateway.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import lombok.Setter;

@Setter
@Configuration
@EnableWebFluxSecurity
@ConfigurationProperties(prefix = "security")
public class WebFluxSecurityConfig {

	// whitelist에 등록된 URL 패턴은 인증 없이 허용됨
	private List<String> whitelist;

	@Bean
	public SecurityWebFilterChain configure(ServerHttpSecurity http) {
		return http
				// 게이트웨이는 세션/폼 기반 인증을 쓰지 않기 때문에 모두 비활성화
				.csrf(ServerHttpSecurity.CsrfSpec::disable)
				.formLogin(ServerHttpSecurity.FormLoginSpec::disable)
				.httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)

				// 요청 URL에 대한 접근 제어 설정 (deprecated 없는 람다 스타일)
				.authorizeExchange(exchanges -> {
					// whitelist가 존재하고 비어있지 않을 때만 pathMatchers 적용
					if (whitelist != null && !whitelist.isEmpty()) {
						exchanges.pathMatchers(whitelist.toArray(String[]::new)).permitAll();
					}
					// 나머지 모든 요청은 인증 없이 허용
					exchanges.anyExchange().permitAll();
				})

				// WebFlux용 Security 필터 체인 생성
				.build();
	}
}
