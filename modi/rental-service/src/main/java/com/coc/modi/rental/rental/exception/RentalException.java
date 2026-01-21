package com.coc.modi.rental.rental.exception;

import com.coc.modi.common.BaseException;
import com.coc.modi.common.ErrorCode;

public class RentalException extends BaseException {
	
	public RentalException(ErrorCode errorCode) {
		
		super(errorCode);
	}
	
	public RentalException(ErrorCode errorCode, String message) {
		
		super(errorCode, message);
	}

	public RentalException(ErrorCode errorCode, String message, Throwable cause) {

		super(errorCode, message, cause);
	}
}
