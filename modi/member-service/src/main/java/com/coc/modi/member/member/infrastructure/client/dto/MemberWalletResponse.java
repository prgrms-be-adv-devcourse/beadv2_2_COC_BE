package com.coc.modi.member.member.infrastructure.client.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MemberWalletResponse(
		BigDecimal balance,
		LocalDateTime createdAt
) {
}
