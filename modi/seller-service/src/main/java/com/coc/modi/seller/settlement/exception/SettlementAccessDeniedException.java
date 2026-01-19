package com.coc.modi.seller.settlement.exception;

import com.coc.modi.common.ErrorCode;

public class SettlementAccessDeniedException extends SettlementException {

	public SettlementAccessDeniedException(String detailMessage) {

		super(ErrorCode.FORBIDDEN, detailMessage);
	}
}
