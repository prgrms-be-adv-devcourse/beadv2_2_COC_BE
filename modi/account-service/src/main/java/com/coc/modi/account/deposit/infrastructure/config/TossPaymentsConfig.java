package com.coc.modi.account.deposit.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "toss.payments")
public class TossPaymentsConfig {

    private String clientKey;
    private String secretKey;
    private String apiUrl;
    private String successUrl;
    private String failUrl;

    // Base64 인코딩된 헤더 생성
    public String getAuthorizationHeader() {

        String credential = secretKey + ":";

        String encoded = java.util.Base64.getEncoder()
                .encodeToString(credential.getBytes(StandardCharsets.UTF_8));

        return "Basic " + encoded;
    }

}
