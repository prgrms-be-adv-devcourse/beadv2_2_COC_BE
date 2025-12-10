package com.coc.modi.account.deposit.presentation.dto;

public record TossConfigResponse(
        String clientKey,
        String successUrl,
        String failUrl
) {
}
