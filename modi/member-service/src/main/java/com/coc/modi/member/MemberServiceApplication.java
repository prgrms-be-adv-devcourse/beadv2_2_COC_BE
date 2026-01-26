package com.coc.modi.member;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.coc.modi.common.auth.InternalApiFeignConfig;

@EnableJpaAuditing
@EnableFeignClients(basePackages = "com.coc.modi", defaultConfiguration = InternalApiFeignConfig.class)
@EnableScheduling
@SpringBootApplication(scanBasePackages = "com.coc.modi")
public class MemberServiceApplication {
	
	public static void main(String[] args) {
		
		SpringApplication.run(MemberServiceApplication.class, args);
	}
}
