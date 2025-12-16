package com.coc.modi.member;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@EnableFeignClients(basePackages = "com.coc.modi")
@SpringBootApplication(scanBasePackages = "com.coc.modi")
public class MemberServiceApplication {
	
	public static void main(String[] args) {
		
		SpringApplication.run(MemberServiceApplication.class, args);
	}
}

