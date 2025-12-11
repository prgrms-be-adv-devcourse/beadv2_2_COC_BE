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
	
	// whiteList에 등록된 URL 패턴은 인증없이 허용됨
	private List<String> whitelist = List.of();
	
	@Bean
	public SecurityWebFilterChain configure(ServerHttpSecurity http) {
		
		return http
				// 게이트웨이는 세션/폼 기반 인증을 쓰지 않기때문에 모두 비활성화
				.csrf(ServerHttpSecurity.CsrfSpec::disable)
				.formLogin(ServerHttpSecurity.FormLoginSpec::disable)
				.httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
				
				// 요청 URL에 대한 접근 제어 설정
				.authorizeExchange(exchanges -> exchanges
						// whitelist에 포함된 경로는 인증없이 통과
						.pathMatchers(whitelist.toArray(String[]::new)).permitAll()
						
						// 나머지 모든 요청도 인증없이 허용
						.anyExchange().permitAll()
				)
				// WebFlux용 Security 필터 체인 생성
				.build();
	}
}
 