package com.coc.gateway.config;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springdoc.core.properties.SwaggerUiConfigParameters;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiDocConfig {
	
	@Bean
	public ApplicationRunner swaggerUiGroupsInitializer(
			ObjectProvider<SwaggerUiConfigParameters> swaggerUiConfigParametersProvider,
			RouteDefinitionLocator locator) {
		
		return args -> {
			
			// Swagger UI 설정 객체 가져오기
			SwaggerUiConfigParameters swaggerUiConfigParameters = swaggerUiConfigParametersProvider.getIfAvailable();
			
			if (swaggerUiConfigParameters == null) {
				
				return;
			}
			
			// 게이트웨이에 등록된 모든 라우트 목록
			List<RouteDefinition> definitions = locator.getRouteDefinitions()
					.collectList()
					.block();
			
			// 동일 서비스 중복 방지
			Set<String> addedGroups = new HashSet<>();
			
			// 라우트 목록에서 -service로 끝나는 서비스만 Swagger 그룹으로 추가
			Optional.ofNullable(definitions).stream()
					.flatMap(Collection::stream)
					.filter(routeDefinition -> routeDefinition.getId().matches(".*-service"))
					.forEach(routeDefinition -> {
						
						String name = routeDefinition.getId();
						
						// 중복 추가 방지
						if (!addedGroups.contains(name)) {
							
							String apiDocsUrl = "/" + name + "/v3/api-docs";
							
							// Swagger UI 그룹에 등록
							swaggerUiConfigParameters.addGroup(name, apiDocsUrl);
							addedGroups.add(name);
							
							System.out.println("Added Swagger group: " + name + " -> " + apiDocsUrl);
						}
					});
		};
	}
}