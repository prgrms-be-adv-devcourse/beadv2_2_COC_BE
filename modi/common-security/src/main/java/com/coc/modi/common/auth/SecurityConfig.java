package com.coc.modi.common.auth;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	private static final String[] SWAGGER_WHITELIST = {
			
			"/swagger-ui/index.html", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/swagger-resources",
			"/webjars/**", "swagger-ui.html"
		
	};
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		
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
							.requestMatchers(HttpMethod.POST, "/api/members/signup").permitAll()
							.requestMatchers(HttpMethod.POST, "/api/auth/**").permitAll()
							.requestMatchers("/ws/**").authenticated()
							.requestMatchers("/internal/**").permitAll()
							.requestMatchers("/actuator/**").permitAll()
							.requestMatchers("/api/**").authenticated()
							.anyRequest().denyAll()
				)
				.addFilterBefore(
						new HeaderAuthenticationFilter(),
						UsernamePasswordAuthenticationFilter.class
				);
		
		return http.build();
	}
}
