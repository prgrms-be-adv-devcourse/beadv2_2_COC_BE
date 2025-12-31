package com.coc.modi.seller.exception;

import com.coc.modi.common.ErrorCode;

public class ChatRoomNotFoundException extends SellerException {

	public ChatRoomNotFoundException(String detailMessage) {
		super(ErrorCode.NOT_FOUND, detailMessage);
	}
}
