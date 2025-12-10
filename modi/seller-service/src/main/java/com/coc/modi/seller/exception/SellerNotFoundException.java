package com.coc.modi.seller.exception;

import com.coc.modi.common.BaseException;
import com.coc.modi.common.ErrorCode;

public class SellerNotFoundException extends BaseException {

    public SellerNotFoundException() {
        super(ErrorCode.NOT_FOUND, "판매자를 찾을 수 없습니다.");
    }

    public SellerNotFoundException(String detailMessage) {
        super(ErrorCode.NOT_FOUND, detailMessage);
    }
}
