package com.coc.modi.common;

import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {
	
	private final ErrorCode errorCode;
	private final String detailMessage;
	
	public BaseException(ErrorCode errorCode) {
		
		this(errorCode, errorCode.getDefaultMessage(), null);
	}
	
	public BaseException(ErrorCode errorCode, String detailMessage) {
		
		this(errorCode, detailMessage, null);
	}
	
	public BaseException(ErrorCode errorCode, String detailMessage, Throwable cause) {
		
		super(detailMessage, cause);
		this.errorCode = errorCode;
		this.detailMessage = detailMessage;
	}
	
	public ErrorCode getErrorCode() {
		
		return errorCode;
	}
	
	public String getDetailMessage() {
		
		return detailMessage;
	}
}
