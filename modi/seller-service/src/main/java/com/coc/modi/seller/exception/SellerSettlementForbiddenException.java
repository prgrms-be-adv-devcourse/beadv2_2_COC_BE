package com.coc.modi.seller.exception;

import com.coc.modi.common.BaseException;
import com.coc.modi.common.ErrorCode;

public class SellerSettlementForbiddenException extends BaseException {

    public SellerSettlementForbiddenException() {
        super(ErrorCode.SELLER_SETTLEMENT_FORBIDDEN, "정산서 소유자가 일치하지 않습니다.");
    }

    public SellerSettlementForbiddenException(String detailMessage) {
        super(ErrorCode.SELLER_SETTLEMENT_FORBIDDEN, detailMessage);
    }
}
