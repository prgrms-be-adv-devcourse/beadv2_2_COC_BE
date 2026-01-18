package com.coc.modi.seller.seller.exception;

import com.coc.modi.common.ErrorCode;

public class SellerStatusConflictException extends SellerException {

	public SellerStatusConflictException(String detailMessage) {

		super(ErrorCode.SELLER_STATUS_CONFLICT, detailMessage);
	}
}
