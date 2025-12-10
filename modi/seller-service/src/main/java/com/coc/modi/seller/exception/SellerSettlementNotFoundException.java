package com.coc.modi.seller.exception;

import com.coc.modi.common.BaseException;
import com.coc.modi.common.ErrorCode;

public class SellerSettlementNotFoundException extends BaseException {

    public SellerSettlementNotFoundException() {
        super(ErrorCode.NOT_FOUND, "정산서를 찾을 수 없습니다.");
    }

    public SellerSettlementNotFoundException(String detailMessage) {
        super(ErrorCode.NOT_FOUND, detailMessage);
    }
}
