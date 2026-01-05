package com.coc.modi.seller.seller.exception;

import com.coc.modi.common.BaseException;
import com.coc.modi.common.ErrorCode;

public class SellerException extends BaseException {
	
	public SellerException(ErrorCode errorCode) {
		
		super(errorCode);
	}
	
	public SellerException(ErrorCode errorCode, String message) {
		
		super(errorCode, message);
	}
}
