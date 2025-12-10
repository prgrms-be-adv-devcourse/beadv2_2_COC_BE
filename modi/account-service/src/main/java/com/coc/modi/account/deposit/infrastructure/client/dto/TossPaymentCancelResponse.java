package com.coc.modi.account.deposit.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TossPaymentCancelResponse(
        String status,

        @JsonProperty("paymentKey")
        String paymentKey,

        @JsonProperty("orderId")
        String orderId,

        @JsonProperty("cancelAmount")
        Long cancelAmount
) {
}
