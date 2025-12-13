package com.coc.modi.seller.exception;

import com.coc.modi.common.ErrorCode;

public class SellerSettlementStatusConflictException extends SettlementException {

	public SellerSettlementStatusConflictException(String detailMessage) {
		super(ErrorCode.CONFLICT, detailMessage);
	}
}
