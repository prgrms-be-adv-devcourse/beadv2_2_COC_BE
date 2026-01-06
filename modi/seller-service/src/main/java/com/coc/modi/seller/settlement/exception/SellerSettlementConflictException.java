package com.coc.modi.seller.settlement.exception;

import com.coc.modi.common.ErrorCode;

public class SellerSettlementConflictException extends SettlementException {

	public SellerSettlementConflictException(String detailMessage) {
		super(ErrorCode.SELLER_SETTLEMENT_CONFLICT, detailMessage);
	}
}
