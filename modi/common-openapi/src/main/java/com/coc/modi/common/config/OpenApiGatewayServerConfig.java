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
	
	@Value("${app.gateway-prefix}")
	private String gatewayPrefix;
	
	@Bean
	public OpenApiCustomizer gatewayServerCustomizer() {
		
		return openApi -> {
			
			openApi.setServers(List.of(new Server().url("/" + gatewayPrefix)));
		};
	}
	
	@Bean
	public OpenAPI openAPI() {
		
		return new OpenAPI()
				.components(new Components().addSecuritySchemes("bearerAuth",
						new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
				.addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
	}
}
