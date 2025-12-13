package com.coc.modi.seller.exception;

import com.coc.modi.common.ErrorCode;

public class SellerSettlementNotFoundException extends SettlementException {
	
	public SellerSettlementNotFoundException(String detailMessage) {
		
		super(ErrorCode.SELLER_SETTLEMENT_NOT_FOUND, detailMessage);
	}
}
