package com.coc.modi.support;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.coc.modi.common.auth.InternalApiFeignConfig;

@EnableScheduling
@EnableJpaAuditing
@EntityScan(basePackages = "com.coc.modi")
@EnableJpaRepositories(basePackages = "com.coc.modi")
@ConfigurationPropertiesScan(basePackages = "com.coc.modi")
@EnableFeignClients(basePackages = "com.coc.modi", defaultConfiguration = InternalApiFeignConfig.class)
@SpringBootApplication(scanBasePackages = "com.coc.modi")
public class SupportServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SupportServiceApplication.class, args);
	}
}
