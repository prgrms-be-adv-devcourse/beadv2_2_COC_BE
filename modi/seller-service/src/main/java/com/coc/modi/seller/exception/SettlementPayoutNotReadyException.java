package com.coc.modi.seller.exception;

import com.coc.modi.common.ErrorCode;

public class SettlementPayoutNotReadyException extends SettlementException {

	public SettlementPayoutNotReadyException(String detailMessage) {
		super(ErrorCode.INTERNAL_ERROR, detailMessage);
	}
}
