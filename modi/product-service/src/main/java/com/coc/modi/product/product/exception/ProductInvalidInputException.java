package com.coc.modi.product.product.exception;

import com.coc.modi.common.ErrorCode;

public class ProductInvalidInputException extends ProductException {
	
	public ProductInvalidInputException(String message) {
		
		super(ErrorCode.PRODUCT_INVALID_INPUT, message);
	}
}
