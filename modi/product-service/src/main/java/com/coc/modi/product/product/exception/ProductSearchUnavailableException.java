package com.coc.modi.product.product.exception;

import com.coc.modi.common.ErrorCode;

public class ProductSearchUnavailableException extends ProductException {
	
	public ProductSearchUnavailableException(String message, Throwable cause) {
		
		super(ErrorCode.PRODUCT_SEARCH_UNAVAILABLE, message, cause);
	}
}
