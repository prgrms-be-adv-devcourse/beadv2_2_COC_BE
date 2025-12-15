package com.coc.modi.account.deposit.application.dto;

public record TossConfigResponse(
        String clientKey,
        String successUrl,
        String failUrl
) {
}
