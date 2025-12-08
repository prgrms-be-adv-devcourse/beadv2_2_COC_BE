package com.coc.modi.account.deposit.infrastructure.client;

import com.coc.modi.account.deposit.infrastructure.client.dto.TossPaymentApprovalRequest;
import com.coc.modi.account.deposit.infrastructure.client.dto.TossPaymentApprovalResponse;
import com.coc.modi.account.deposit.infrastructure.client.dto.TossPaymentCancelRequest;
import com.coc.modi.account.deposit.infrastructure.client.dto.TossPaymentCancelResponse;
import com.coc.modi.account.deposit.infrastructure.config.TossPaymentsConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class TossPaymentsClient {

    private final TossPaymentsConfig tossPaymentsConfig;
    private final RestTemplate restTemplate;

    // Toss 결제 승인 API 호출
    public TossPaymentApprovalResponse approvePayment(String paymentKey,
                                                      String orderId,
                                                      BigDecimal amount) {

        // 1. 요청 URL
        String url = tossPaymentsConfig.getApiUrl() + "/confirm";

        // 2. 요청 헤더
        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", tossPaymentsConfig.getAuthorizationHeader());

        // 3. 요청 바디
        TossPaymentApprovalRequest request = new TossPaymentApprovalRequest(
                paymentKey,
                orderId,
                amount
        );

        // 4. HTTP 요청
        HttpEntity<TossPaymentApprovalRequest> entity = new HttpEntity<>(request, headers);

        // 5. API 호출 및 응답 반환
        return restTemplate.postForObject(url, entity, TossPaymentApprovalResponse.class);
    }

    // Toss 결제 취소
    public TossPaymentCancelResponse cancelPayment(String paymentKey,
                                                   BigDecimal cancelAmount,
                                                   String cancelReason) {

        String url = tossPaymentsConfig.getApiUrl() + "/" + paymentKey + "/cancel";

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", tossPaymentsConfig.getAuthorizationHeader());

        TossPaymentCancelRequest request = new TossPaymentCancelRequest(
                cancelReason,
                cancelAmount
        );

        HttpEntity<TossPaymentCancelRequest> entity = new HttpEntity<>(request, headers);

        return restTemplate.postForObject(url, entity, TossPaymentCancelResponse.class);
    }

}
