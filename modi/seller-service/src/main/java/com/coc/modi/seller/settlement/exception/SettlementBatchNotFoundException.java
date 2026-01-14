package com.coc.modi.seller.settlement.exception;

import com.coc.modi.common.ErrorCode;

public class SettlementBatchNotFoundException extends SettlementException {
	
	public SettlementBatchNotFoundException(String detailMessage) {
		
		super(ErrorCode.SETTLEMENT_BATCH_NOT_FOUND, detailMessage);
	}
}
