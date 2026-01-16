package com.coc.modi.seller.seller.exception;

import com.coc.modi.common.ErrorCode;

public class SellerDuplicateException extends SellerException {
	
	public SellerDuplicateException(String detailMessage) {
		
		super(ErrorCode.SELLER_DUPLICATE, detailMessage);
	}
}
