package com.coc.modi.seller;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableJpaAuditing
@SpringBootApplication
@ComponentScan(basePackages = "com.coc.modi")
@EnableFeignClients(basePackages = "com.coc.modi")
@EnableScheduling
@OpenAPIDefinition(
        info = @Info(title = "Seller Service API", version = "1.0")
)
public class SellerServiceApplication {
	
	public static void main(String[] args) {
		
		SpringApplication.run(SellerServiceApplication.class, args);
	}
}
