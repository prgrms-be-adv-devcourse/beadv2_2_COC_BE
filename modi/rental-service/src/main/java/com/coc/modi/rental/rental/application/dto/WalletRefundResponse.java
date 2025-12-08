package com.coc.modi.rental.rental.application.dto;

import java.math.BigDecimal;

public record WalletRefundResponse(
        BigDecimal refundAmount,
        BigDecimal balance
) {
}
