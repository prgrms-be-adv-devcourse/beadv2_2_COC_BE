package com.coc.modi.product.product.exception;

import com.coc.modi.common.ErrorCode;

public class ProductNotFoundException extends ProductException {
	
	public ProductNotFoundException(Long productId) {
		
		super(ErrorCode.PRODUCT_NOT_FOUND, "상품을 찾을 수 없습니다. 상품 ID: " + productId);
	}
}
