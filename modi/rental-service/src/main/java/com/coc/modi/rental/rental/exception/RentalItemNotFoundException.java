package com.coc.modi.rental.rental.exception;

import com.coc.modi.common.ErrorCode;

public class RentalItemNotFoundException extends RentalException {
	
	public RentalItemNotFoundException(Long rentalItemId) {
		
		super(ErrorCode.RENTAL_ITEM_NOT_FOUND, "대여 아이템 정보를 찾을 수 없습니다. rentalItemId: " + rentalItemId);
	}
}
