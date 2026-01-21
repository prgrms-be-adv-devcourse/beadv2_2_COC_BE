package com.coc.modi.seller.settlement.exception;

import com.coc.modi.common.ErrorCode;

public class SettlementPeriodResolveException extends SettlementException {

	public SettlementPeriodResolveException(String detailMessage) {
		super(ErrorCode.INVALID_INPUT, detailMessage);
	}
}
