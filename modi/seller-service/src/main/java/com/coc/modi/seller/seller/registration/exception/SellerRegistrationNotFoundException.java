package com.coc.modi.seller.seller.registration.exception;

import com.coc.modi.common.ErrorCode;
import com.coc.modi.seller.seller.exception.SellerException;

public class SellerRegistrationNotFoundException extends SellerException {

	public SellerRegistrationNotFoundException(String detailMessage) {
		super(ErrorCode.SELLER_NOT_FOUND, detailMessage);
	}
}
