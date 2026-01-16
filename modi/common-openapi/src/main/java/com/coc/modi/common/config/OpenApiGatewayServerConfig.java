package com.coc.modi.common.config;

import java.util.List;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiGatewayServerConfig {
	
	private static final String BEARER_AUTH_SCHEME = "bearerAuth";
	private static final String INTERNAL_AUTH_SCHEME = "internalApiToken";
	
	@Value("${app.gateway-prefix}")
	private String gatewayPrefix;
	
	@Value("${internal.api.header:X-Internal-Token}")
	private String internalHeaderName;
	
	@Bean
	public OpenApiCustomizer gatewayServerCustomizer() {
		
		return openApi -> {
			
			openApi.setServers(List.of(new Server().url("/" + gatewayPrefix)));
		};
	}
	
	@Bean
	public OpenApiCustomizer internalApiCustomizer() {
		
		return openApi -> {
			if (openApi.getPaths() == null) {
				return;
			}
			
			openApi.getPaths().forEach((path, pathItem) -> {
				if (path.startsWith("/internal")) {
					SecurityRequirement requirement = new SecurityRequirement().addList(INTERNAL_AUTH_SCHEME);
					pathItem.readOperations().forEach(operation -> operation.addSecurityItem(requirement));
				}
			});
		};
	}
	
	@Bean
	public OpenAPI openAPI() {
		
		Components components = new Components()
				.addSecuritySchemes(BEARER_AUTH_SCHEME,
						new SecurityScheme()
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT"))
				.addSecuritySchemes(INTERNAL_AUTH_SCHEME,
						new SecurityScheme()
								.type(SecurityScheme.Type.APIKEY)
								.in(SecurityScheme.In.HEADER)
								.name(internalHeaderName));
		
		return new OpenAPI()
				.components(components)
				.addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH_SCHEME));
	}
}
