package com.coc.modi.seller.exception;

import com.coc.modi.common.BaseException;
import com.coc.modi.common.ErrorCode;

public class SellerDuplicateException extends BaseException {

    public SellerDuplicateException() {
        super(ErrorCode.SELLER_DUPLICATE, "이미 등록된 판매자입니다.");
    }

    public SellerDuplicateException(String detailMessage) {
        super(ErrorCode.SELLER_DUPLICATE, detailMessage);
    }
}
