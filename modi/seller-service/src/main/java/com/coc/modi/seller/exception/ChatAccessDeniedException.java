package com.coc.modi.seller.exception;

import com.coc.modi.common.ErrorCode;

public class ChatAccessDeniedException extends SellerException {

	public ChatAccessDeniedException(String detailMessage) {
		super(ErrorCode.FORBIDDEN, detailMessage);
	}
}
