package com.coc.modi.common.auth;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	private static final String[] SWAGGER_WHITELIST = {
			
			"/swagger-ui/index.html", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/swagger-resources",
			"/webjars/**", "swagger-ui.html"
		
	};
	private static final String[] AUTH_PUBLIC_POST_ENDPOINTS = {
			"/api/auth/login",
			"/api/auth/email/verify/send",
			"/api/auth/email/verify/confirm",
			"/api/auth/password/reset/send",
			"/api/auth/password/reset/confirm",
			"/api/auth/reissue",
			"/api/auth/logout",
			"/api/auth/oauth2/signup"
	};
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http,
												   InternalApiAuthenticationFilter internalApiAuthenticationFilter,
												   HeaderAuthenticationFilter headerAuthenticationFilter) throws Exception {
		
		http.httpBasic(AbstractHttpConfigurer::disable)
				// .cors(configurer -> {
				// 	CorsConfiguration configuration = new CorsConfiguration();
				// 	configuration.setAllowedOriginPatterns(List.of("*"));
				// 	configuration.setAllowedMethods(List.of(HttpMethod.POST.name(), HttpMethod.GET.name(), HttpMethod.PUT.name(), HttpMethod.DELETE.name(), HttpMethod.PATCH.name()));
				// 	configuration.addAllowedHeader("*");
				// 	configuration.setAllowCredentials(true);
				// 	configuration.setMaxAge(3600L);
				//
				// 	UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
				// 	source.registerCorsConfiguration("/**", configuration);
				// 	configurer.configurationSource(source);
				// })
				.cors(AbstractHttpConfigurer::disable)
				.csrf(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(auth ->
					auth.requestMatchers(SWAGGER_WHITELIST).permitAll()
							.requestMatchers("/toss-payment.html").permitAll()
							.requestMatchers("/payments/**").permitAll()
							.requestMatchers(HttpMethod.GET, "/api/products/search").permitAll()
							.requestMatchers(HttpMethod.GET, "/api/products/popular-keywords").permitAll()
							.requestMatchers(HttpMethod.GET, "/api/products/popular-products").permitAll()
							.requestMatchers(new RegexRequestMatcher("^/api/products/\\d+$", HttpMethod.GET.name())).permitAll()
							.requestMatchers(new RegexRequestMatcher("^/api/sellers/\\d+$", HttpMethod.GET.name())).permitAll()
							.requestMatchers(HttpMethod.POST, "/api/members/signup").permitAll()
							.requestMatchers(HttpMethod.POST, "/api/auth/oauth2/connect").authenticated()
							.requestMatchers(HttpMethod.POST, AUTH_PUBLIC_POST_ENDPOINTS).permitAll()
							.requestMatchers(
									"/oauth2/**",
									"/login/oauth2/**",
									"/member-service/oauth2/**",
									"/member-service/login/oauth2/**"
							).permitAll()
							.requestMatchers(HttpMethod.GET, "/api/notices/**").permitAll()
							.requestMatchers("/internal/**").hasRole("INTERNAL")
							.requestMatchers("/ws/**").authenticated()
							.requestMatchers("/actuator/**").permitAll()
							.requestMatchers("/api/**").authenticated()
							.anyRequest().denyAll()
				)
				.addFilterBefore(
						internalApiAuthenticationFilter,
						UsernamePasswordAuthenticationFilter.class
				)
				.addFilterBefore(
						headerAuthenticationFilter,
						UsernamePasswordAuthenticationFilter.class
				);
		
		return http.build();
	}
	
	@Bean
	public HeaderAuthenticationFilter headerAuthenticationFilter() {
		
		return new HeaderAuthenticationFilter();
	}
	
	@Bean
	public InternalApiAuthenticationFilter internalApiAuthenticationFilter(
			@Value("${internal.api.token:}") String token,
			@Value("${internal.api.header:X-Internal-Token}") String headerName
	) {
		
		return new InternalApiAuthenticationFilter(token, headerName);
	}
}
