package com.coc.modi.seller.settlement.exception;

import com.coc.modi.common.ErrorCode;

public class SellerSettlementStatusConflictException extends SettlementException {

	public SellerSettlementStatusConflictException(String detailMessage) {
		super(ErrorCode.CONFLICT, detailMessage);
	}
}
