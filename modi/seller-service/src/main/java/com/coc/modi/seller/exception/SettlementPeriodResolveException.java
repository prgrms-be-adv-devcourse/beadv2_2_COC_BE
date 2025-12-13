package com.coc.modi.seller.exception;

import com.coc.modi.common.ErrorCode;

public class SettlementPeriodResolveException extends SettlementException {

	public SettlementPeriodResolveException(String detailMessage) {
		super(ErrorCode.INVALID_INPUT, detailMessage);
	}
}
