package com.coc.gateway.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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


import com.coc.gateway.security.GlobalAuthHeaderFilter;
import com.coc.gateway.security.JwtTokenProvider;
import com.coc.gateway.security.authz.AuthzRequestContext;
import com.coc.gateway.security.authz.MemberAuthzService;
import com.coc.gateway.security.permission.EndpointPermissionAuthorizationManager;

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
				"http://localhost:8000", //local test
				"http://localhost:3000",
				"https://*.v0.app",
				"https://*.vusercontent.net",
				"https://*.vercel.app",
				"https://www.cocmodi.shop",

				//gateway
				"https://localhost:8080",

				//tunneling
				"https://cocmodi.shop"
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
	public GlobalAuthHeaderFilter globalAuthHeaderFilter() {
		
		return new GlobalAuthHeaderFilter();
	}
	
	@Bean
	public SecurityWebFilterChain securityWebFilterChain(
			
			ServerHttpSecurity http,
			AuthenticationWebFilter jwtAuthWebFilter,
			GlobalAuthHeaderFilter globalAuthHeaderFilter,
			EndpointPermissionAuthorizationManager endpointPermissionAuthorizationManager) {
		
		return http
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.csrf(ServerHttpSecurity.CsrfSpec::disable)
				.formLogin(ServerHttpSecurity.FormLoginSpec::disable)
				.httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
				.authorizeExchange(ex -> ex
						.pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.pathMatchers(
								"/swagger-ui/**",
								"/v3/api-docs/**",
								"/*/v3/api-docs/**",
								"/member-service/api/auth/**",
								"/member-service/api/members/signup",
								"/favicon.ico"
						).permitAll()
						.pathMatchers(whitelist.toArray(new String[0])).permitAll()
						.pathMatchers("/*/actuator/**").permitAll()
						.anyExchange().access(endpointPermissionAuthorizationManager)
				)
				.addFilterAt(jwtAuthWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
				.addFilterAfter(globalAuthHeaderFilter, SecurityWebFiltersOrder.AUTHENTICATION)
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
			String method = exchange.getRequest().getMethod().toString();
			String path = exchange.getRequest().getPath().pathWithinApplication().value();
			
			UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(token, token);
			authToken.setDetails(new AuthzRequestContext(method, path));
			
			return Mono.just(authToken);
		});
		
		filter.setAuthenticationFailureHandler((webFilterExchange, exception) ->
				Mono.fromRunnable(() -> webFilterExchange.getExchange().getResponse().setStatusCode(HttpStatus.UNAUTHORIZED)));
		
		return filter;
	}
	
	@Bean
	public ReactiveAuthenticationManager jwtAuthenticationManager(
			JwtTokenProvider jwtTokenProvider,
			MemberAuthzService memberAuthzService) {
		
		return authentication -> {
			
			String token = (String) authentication.getCredentials();
			
			if (!jwtTokenProvider.validateToken(token)) {
				
				return Mono.error(new BadCredentialsException("Invalid JWT token"));
			}
			
			Long memberId = jwtTokenProvider.getMemberId(token);
			AuthzRequestContext context = authentication.getDetails() instanceof AuthzRequestContext details
					? details
					: new AuthzRequestContext("", "");
			
			return memberAuthzService.resolveRoles(memberId, context.method(), context.path())
					.defaultIfEmpty(Set.of())
					.map(roles -> {
						List<GrantedAuthority> authorities = new ArrayList<>();
						for (String role : roles) {
							authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
						}
						return new UsernamePasswordAuthenticationToken(memberId, token, authorities);
					});
		};
	}
}
 
