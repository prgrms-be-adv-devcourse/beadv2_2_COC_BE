package com.coc.modi.seller.settlement.exception;

import com.coc.modi.common.ErrorCode;

public class SettlementBatchDuplicateException extends SettlementException {
	
	public SettlementBatchDuplicateException(String detailMessage) {
		
		super(ErrorCode.SETTLEMENT_BATCH_DUPLICATE, detailMessage);
	}
}
