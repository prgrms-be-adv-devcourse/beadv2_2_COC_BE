package com.coc.modi.product.product.exception;

import com.coc.modi.common.ErrorCode;

public class ProductAccessDeniedException extends ProductException {
	
	public ProductAccessDeniedException(String action) {
		
		super(ErrorCode.PRODUCT_FORBIDDEN, "해당 상품의 " + action + " 권한이 없습니다.");
	}
}
