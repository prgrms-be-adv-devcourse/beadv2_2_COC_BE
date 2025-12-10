package com.coc.modi.rental.rental.exception;

import com.coc.modi.common.ErrorCode;

public class RentalNotFoundException extends RentalException {
	
	public RentalNotFoundException(Long rentalId) {
		
		super(ErrorCode.RENTAL_NOT_FOUND, "렌탈 정보를 찾을 수 없습니다. rentalId: " + rentalId);
	}
}
