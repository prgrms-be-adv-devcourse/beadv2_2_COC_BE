package com.coc.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiDocConfig {
	
	@Bean
	public OpenAPI openAPI() {
		
		return new OpenAPI()
				.components(new Components().addSecuritySchemes("bearerAuth",
						new SecurityScheme()
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")
				))
				.addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
	}
	
/*
	@Bean
	public List<GroupedOpenApi> apis(
			ObjectProvider<SwaggerUiConfigParameters> swaggerUiConfigParametersProvider,
			RouteDefinitionLocator locator
	) {
		List<GroupedOpenApi> groupedOpenApis = new ArrayList<>();

		List<RouteDefinition> definitions = locator.getRouteDefinitions()
				.collectList()
				.blockOptional()
				.orElse(List.of());

		SwaggerUiConfigParameters swaggerParams = swaggerUiConfigParametersProvider.getIfAvailable();

		definitions.stream()
				.map(RouteDefinition::getId)
				.filter(id -> id != null && id.endsWith("-service"))
				.forEach(serviceId -> {

					// ✅ swagger-ui가 여러 서비스 문서를 “링크 목록”으로 띄우게 하는 설정
					if (swaggerParams != null) {
						swaggerParams.addGroup(serviceId, "/v3/api-docs/" + serviceId);
					}

					// ✅ (선택) 그룹 OpenAPI 빈도 등록 (그룹이 안 뜨면 도움 됨)
					groupedOpenApis.add(
							GroupedOpenApi.builder()
									.group(serviceId)
									.pathsToMatch("/" + serviceId + "/**")
									.build()
					);
				});

		return groupedOpenApis;
	}*/
}