package com.coc.modi.account.deposit.presentation.dto;

import com.coc.modi.account.deposit.application.dto.DepositApprovalCommand;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DepositApprovalRequest(
		@NotBlank(message = "paymentKey는 필수입니다.")
		String paymentKey,
		
		@NotBlank(message = "orderId는 필수입니다.")
		String orderId,
		
		@NotNull(message = "amount는 필수입니다.")
		@Positive(message = "amount는 0보다 커야 합니다.")
		BigDecimal amount
) {

    public DepositApprovalCommand toCommand() {

        return new DepositApprovalCommand(
                paymentKey,
                orderId,
                amount
        );
    }
}
