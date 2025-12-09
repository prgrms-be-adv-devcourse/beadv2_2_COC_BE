package com.coc.modi.member;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaAuditing

@SecurityScheme(
		name = "BearerAuth",
		type = SecuritySchemeType.HTTP,
		bearerFormat = "JWT",
		scheme = "bearer"
)
@EntityScan(basePackages = "com.coc.modi")
@EnableFeignClients(basePackages = "com.coc.modi")
@EnableJpaRepositories(basePackages = "com.coc.modi")
@SpringBootApplication(scanBasePackages = "com.coc.modi")
@OpenAPIDefinition(
		info = @Info(title = "Member Service API", version = "1.0"),
		security = @SecurityRequirement(name = "BearerAuth")
)
public class MemberServiceApplication {
	
	public static void main(String[] args) {
		
		SpringApplication.run(MemberServiceApplication.class, args);
	}
}

