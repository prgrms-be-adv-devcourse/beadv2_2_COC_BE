package com.coc.modi.seller.settlement.exception;

import com.coc.modi.common.ErrorCode;

public class SettlementInputInvalidException extends SettlementException {

	public SettlementInputInvalidException(String detailMessage) {
		super(ErrorCode.INVALID_INPUT, detailMessage);
	}

	public SettlementInputInvalidException(String detailMessage, Throwable cause) {
		super(ErrorCode.INVALID_INPUT, detailMessage, cause);
	}
}
