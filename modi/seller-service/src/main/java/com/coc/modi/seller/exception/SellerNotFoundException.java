package com.coc.modi.seller.exception;

import com.coc.modi.common.BaseException;
import com.coc.modi.common.ErrorCode;

public class SellerNotFoundException extends BaseException {

    public SellerNotFoundException(String detailMessage) {
        super(ErrorCode.SELLER_NOT_FOUND, detailMessage);
    }
}
