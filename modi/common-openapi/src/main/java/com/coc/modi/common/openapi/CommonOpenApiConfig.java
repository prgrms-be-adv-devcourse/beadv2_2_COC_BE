package com.coc.modi.common.openapi;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
@EnableConfigurationProperties(OpenApiProperties.class)
public class CommonOpenApiConfig {
	
	private final OpenApiProperties properties;
	
	public CommonOpenApiConfig(OpenApiProperties properties) {
		
		this.properties = properties;
	}
	
	@Bean
	public OpenAPI openAPI(@Value("${spring.application.name}") String appName) {
		
		// base-path: 설정 없으면 /{appName} 사용
		String basePath = Optional.ofNullable(properties.getBasePath())
				.orElse("/" + appName);
		
		String title = Optional.ofNullable(properties.getTitle())
				.orElse(appName + " API");
		
		String version = Optional.ofNullable(properties.getVersion())
				.orElse("1.0.0");
		
		Server server = new Server().url(basePath);
		
		return new OpenAPI()
				.info(new Info()
						.title(title)
						.version(version))
				.servers(List.of(server));
	}
}
