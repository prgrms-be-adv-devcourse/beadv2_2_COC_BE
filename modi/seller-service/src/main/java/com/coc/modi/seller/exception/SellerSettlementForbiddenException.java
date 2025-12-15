package com.coc.modi.seller.exception;

import com.coc.modi.common.ErrorCode;

public class SellerSettlementForbiddenException extends SettlementException {
	
	public SellerSettlementForbiddenException(String detailMessage) {
		
		super(ErrorCode.SELLER_SETTLEMENT_FORBIDDEN, detailMessage);
	}
}
