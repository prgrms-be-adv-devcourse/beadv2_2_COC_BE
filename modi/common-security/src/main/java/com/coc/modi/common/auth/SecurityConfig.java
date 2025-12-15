package com.coc.modi.common.auth;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	
	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	
	private static final String[] SWAGGER_WHITELIST = {
			
			"/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/swagger-resources",
			"/webjars/**"
		
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
				.httpBasic(AbstractHttpConfigurer::disable)
//				.cors(AbstractHttpConfigurer::disable)
				.csrf(Customizer.withDefaults())
				.formLogin(AbstractHttpConfigurer::disable)
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
				.authorizeHttpRequests(auth -> {
					auth.requestMatchers(SWAGGER_WHITELIST).permitAll()
							.requestMatchers("/internal/**").permitAll()
							.requestMatchers("/api/**").permitAll()
							.requestMatchers("/actuator/**").permitAll()
							.anyRequest().authenticated();
				});
		
		return http.build();
	}
}
