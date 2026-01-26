package com.coc.modi.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import com.coc.modi.common.auth.InternalApiFeignConfig;

@EnableJpaAuditing
@EnableFeignClients(basePackages = "com.coc.modi", defaultConfiguration = InternalApiFeignConfig.class)
@SpringBootApplication(scanBasePackages = "com.coc.modi")
public class ProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
