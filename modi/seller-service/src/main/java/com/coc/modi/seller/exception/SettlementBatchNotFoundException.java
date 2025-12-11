package com.coc.modi.seller.exception;

import com.coc.modi.common.BaseException;
import com.coc.modi.common.ErrorCode;

public class SettlementBatchNotFoundException extends BaseException {

    public SettlementBatchNotFoundException() {
        super(ErrorCode.SETTLEMENT_BATCH_NOT_FOUND, "정산 배치를 찾을 수 없습니다.");
    }

    public SettlementBatchNotFoundException(String detailMessage) {
        super(ErrorCode.SETTLEMENT_BATCH_NOT_FOUND, detailMessage);
    }
}
