package com.coc.modi.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import com.coc.modi.common.auth.InternalApiFeignConfig;

@EnableJpaAuditing
@EnableFeignClients(basePackages = "com.coc.modi", defaultConfiguration = InternalApiFeignConfig.class)
@SpringBootApplication(scanBasePackages = "com.coc.modi")
public class AiServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiServiceApplication.class, args);
    }
}
