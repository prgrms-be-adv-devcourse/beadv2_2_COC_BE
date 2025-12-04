package com.coc.modi.account.wallet.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MemberWalletResponse (
        BigDecimal balance,
        LocalDateTime createdAt
) {

}
