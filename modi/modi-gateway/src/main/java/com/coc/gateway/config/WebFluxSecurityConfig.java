package com.coc.gateway.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.server.SecurityWebFilterChain;

import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import com.coc.gateway.security.JwtTokenProvider;

import lombok.Setter;
import reactor.core.publisher.Mono;

@Setter
@Configuration
@EnableWebFluxSecurity
@ConfigurationProperties(prefix = "security")
public class WebFluxSecurityConfig {
	
	// whiteList에 등록된 URL 패턴은 인증없이 허용됨
	private List<String> whitelist = List.of();
	
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOriginPatterns(List.of(
				"http://localhost:3000",
				"https://*.v0.app",
				"https://*.vusercontent.net",
				"https://*.vercel.app",
				"https://localhost:8000",
				"https://*.ngrok-free.dev",
				"https://*.ngrok.free.app"
		));
		
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
		config.addAllowedHeader("*");
		config.setAllowCredentials(true);
		config.setMaxAge(3600L);
		
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		
		return source;
	}
	
	@Bean
	public SecurityWebFilterChain securityWebFilterChain(
			ServerHttpSecurity http,
			AuthenticationWebFilter jwtAuthWebFilter
			
	) {
		
		return http
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.csrf(ServerHttpSecurity.CsrfSpec::disable)
				.formLogin(ServerHttpSecurity.FormLoginSpec::disable)
				.httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
				.authorizeExchange(ex -> ex
						.pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.pathMatchers(whitelist.toArray(new String[0])).permitAll()
						.anyExchange().authenticated()
				)
				.addFilterAt(jwtAuthWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
				.build();
	}
	
	@Bean
	public AuthenticationWebFilter jwtAuthWebFilter(ReactiveAuthenticationManager jwtAuthenticationManager) {
		
		AuthenticationWebFilter filter = new AuthenticationWebFilter(jwtAuthenticationManager);
		
		filter.setServerAuthenticationConverter(exchange -> {
			String auth = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
			
			if (auth == null || !auth.startsWith("Bearer ")) {
				
				return Mono.empty();
			}
			
			String token = auth.substring(7);
			
			return Mono.just(new UsernamePasswordAuthenticationToken(token, token));
		});
		
		filter.setAuthenticationFailureHandler((webFilterExchange, exception) ->
				Mono.fromRunnable(() -> webFilterExchange.getExchange().getResponse().setStatusCode(HttpStatus.UNAUTHORIZED)));
		
		return filter;
	}
	
	@Bean
	public ReactiveAuthenticationManager jwtAuthenticationManager(JwtTokenProvider jwtTokenProvider) {
		
		return authentication -> {
			
			String token = (String) authentication.getCredentials();
			
			if (!jwtTokenProvider.validateToken(token)) {
				
				return Mono.error(new BadCredentialsException("Invalid JWT token"));
			}
			
			Long memberId = jwtTokenProvider.getMemberId(token);
			String role = jwtTokenProvider.getRole(token);
			GrantedAuthority authority = new SimpleGrantedAuthority(role);
			
			return Mono.just(new UsernamePasswordAuthenticationToken(memberId, token, List.of(authority)));
		};
	}
}
 