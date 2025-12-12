package com.coc.modi.product.product.exception;

import com.coc.modi.common.ErrorCode;

public class ProductConflictException extends ProductException {
	
	public ProductConflictException(String message) {
		
		super(ErrorCode.PRODUCT_CONFLICT, message);
	}
}
