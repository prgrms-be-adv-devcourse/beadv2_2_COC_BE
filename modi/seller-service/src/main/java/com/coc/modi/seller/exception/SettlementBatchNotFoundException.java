package com.coc.modi.seller.exception;

import com.coc.modi.common.BaseException;
import com.coc.modi.common.ErrorCode;

public class SettlementBatchNotFoundException extends BaseException {

    public SettlementBatchNotFoundException(String detailMessage) {
        super(ErrorCode.SETTLEMENT_BATCH_NOT_FOUND, detailMessage);
    }
}
