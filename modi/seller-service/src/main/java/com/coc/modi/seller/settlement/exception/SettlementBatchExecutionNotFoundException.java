package com.coc.modi.seller.settlement.exception;

import com.coc.modi.common.ErrorCode;

public class SettlementBatchExecutionNotFoundException extends SettlementException {
	
	public SettlementBatchExecutionNotFoundException(String detailMessage) {
		
		super(ErrorCode.SETTLEMENT_BATCH_EXECUTION_NOT_FOUND, detailMessage);
	}
}
