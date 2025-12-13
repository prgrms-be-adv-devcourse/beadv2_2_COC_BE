package com.coc.modi.common;

public class UnauthorizedException extends BaseException {
	
	public UnauthorizedException() {
		super(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.getDefaultMessage());
	}
	
	public UnauthorizedException(String message) {
		super(ErrorCode.UNAUTHORIZED, message);
	}
}
