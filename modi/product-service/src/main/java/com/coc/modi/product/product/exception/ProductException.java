package com.coc.modi.product.product.exception;

import com.coc.modi.common.BaseException;
import com.coc.modi.common.ErrorCode;

public class ProductException extends BaseException {
	
	public ProductException(ErrorCode errorCode, String message) {
		
		super(errorCode, message);
	}
	
	public ProductException(ErrorCode errorCode, String message, Throwable cause) {
		
		super(errorCode, message, cause);
	}
}
