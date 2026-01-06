package com.coc.modi.seller.seller.exception;

import com.coc.modi.common.ErrorCode;

public class SellerNotFoundException extends SellerException {
	
	public SellerNotFoundException(String detailMessage) {
		
		super(ErrorCode.SELLER_NOT_FOUND, detailMessage);
	}
}
