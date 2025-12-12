package com.coc.modi.common.openapi;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "openapi")
public class OpenApiProperties {
	
	/**
	 * Swagger 문서 타이틀 (없으면 spring.application.name 기반으로 생성)
	 */
	private String title;
	
	/**
	 * API 버전 (없으면 기본값 1.0.0)
	 */
	private String version = "1.0.0";
	
	/**
	 * Gateway 기준 base-path (없으면 "/{spring.application.name}" 사용)
	 * ex) /member-service, /account-service
	 */
	private String basePath;
 
}
