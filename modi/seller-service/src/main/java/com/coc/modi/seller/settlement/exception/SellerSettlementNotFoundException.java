package com.coc.modi.seller.settlement.exception;

import com.coc.modi.common.ErrorCode;

public class SellerSettlementNotFoundException extends SettlementException {
	
	public SellerSettlementNotFoundException(String detailMessage) {
		
		super(ErrorCode.SELLER_SETTLEMENT_NOT_FOUND, detailMessage);
	}
}
