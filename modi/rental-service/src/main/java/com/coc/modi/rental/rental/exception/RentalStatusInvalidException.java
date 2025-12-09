package com.coc.modi.rental.rental.exception;

import com.coc.modi.common.ErrorCode;

public class RentalStatusInvalidException extends RentalException {
	
	public RentalStatusInvalidException(String message) {
		
		super(ErrorCode.RENTAL_STATUS_INVALID, message);
	}
}
