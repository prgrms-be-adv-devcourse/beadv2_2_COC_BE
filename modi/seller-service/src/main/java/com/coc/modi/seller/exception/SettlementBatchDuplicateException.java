package com.coc.modi.seller.exception;

import com.coc.modi.common.BaseException;
import com.coc.modi.common.ErrorCode;

public class SettlementBatchDuplicateException extends BaseException {

    public SettlementBatchDuplicateException(String detailMessage) {
        super(ErrorCode.SETTLEMENT_BATCH_DUPLICATE, detailMessage);
    }
}
