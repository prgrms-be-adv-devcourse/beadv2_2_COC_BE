package com.coc.modi.product.product.exception;

import com.coc.modi.common.ErrorCode;

public class ProductEmbeddingOutboxException extends ProductException {
	
	public ProductEmbeddingOutboxException(String message, Throwable cause) {
		
		super(ErrorCode.PRODUCT_INTERNAL_ERROR, message, cause);
	}
}
