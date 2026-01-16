package com.coc.modi.seller.settlement.exception;

import com.coc.modi.common.ErrorCode;

public class SettlementBatchRunException extends SettlementException {

	public SettlementBatchRunException(String detailMessage, Throwable cause) {
		super(ErrorCode.INTERNAL_ERROR, detailMessage, cause);
	}
}
