package com.coc.modi.account.wallet.application.dto;

import com.coc.modi.account.wallet.domain.MemberWallet;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MemberWalletResponse (
        BigDecimal balance,
        LocalDateTime createdAt
) {
    public static MemberWalletResponse from(MemberWallet wallet) {
        return new MemberWalletResponse(
                wallet.getBalance(),
                wallet.getCreatedAt()
        );
    }
}
