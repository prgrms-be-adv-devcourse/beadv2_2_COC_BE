package com.coc.modi.seller.exception;

import com.coc.modi.common.BaseException;
import com.coc.modi.common.ErrorCode;

public class SettlementBatchDuplicateException extends BaseException {

    public SettlementBatchDuplicateException() {
        super(ErrorCode.CONFLICT, "이미 생성된 정산 배치입니다.");
    }

    public SettlementBatchDuplicateException(String detailMessage) {
        super(ErrorCode.CONFLICT, detailMessage);
    }
}
