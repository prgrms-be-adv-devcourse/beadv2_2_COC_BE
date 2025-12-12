package com.coc.modi.member.member.exception;

import com.coc.modi.common.ErrorCode;

public class AddressNotFoundException extends MemberException {
	
	public AddressNotFoundException(Long addressId) {
		
		super(ErrorCode.ADDRESS_NOT_FOUND, "주소 정보를 찾을 수 없습니다. addressId=" + addressId);
	}
}
