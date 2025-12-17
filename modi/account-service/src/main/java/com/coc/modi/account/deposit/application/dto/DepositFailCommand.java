package com.coc.modi.account.deposit.application.dto;

public record DepositFailCommand(
		String orderId,
		String failureCode,
		String failureMessage
) { }
