package com.coc.modi.account.deposit.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TossPaymentApprovalResponse (
        String paymentKey,
        String orderId,
        String status,
        Long totalAmount,
        String approvedAt,
        String method,

        @JsonProperty("card")
        Card card,

        @JsonProperty("failure")
        Failure failure

){
    public record Card(
            String company,
            String number,
            String approveNo
    ){

    }

    public record Failure(
            String code,
            String message
    ){

    }

}
