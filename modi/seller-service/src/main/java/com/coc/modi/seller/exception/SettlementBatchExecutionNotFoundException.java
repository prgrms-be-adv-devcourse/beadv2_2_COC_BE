package com.coc.modi.seller.exception;

import com.coc.modi.common.BaseException;
import com.coc.modi.common.ErrorCode;

public class SettlementBatchExecutionNotFoundException extends BaseException {

    public SettlementBatchExecutionNotFoundException(String detailMessage) {
        super(ErrorCode.SETTLEMENT_BATCH_EXECUTION_NOT_FOUND, detailMessage);
    }
}
