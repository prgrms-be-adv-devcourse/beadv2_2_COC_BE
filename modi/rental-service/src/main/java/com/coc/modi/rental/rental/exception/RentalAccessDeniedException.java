package com.coc.modi.rental.rental.exception;

import com.coc.modi.common.ErrorCode;

public class RentalAccessDeniedException extends RentalException {
	
	public RentalAccessDeniedException(String message) {
		
		super(ErrorCode.RENTAL_MEMBER_MISMATCH, message);
	}
	
	public static RentalAccessDeniedException memberMismatch(Long rentalId, Long memberId) {
		
		return new RentalAccessDeniedException("대여 요청자와 요청 멤버가 일치하지 않습니다. rentalId: " + rentalId + ", memberId: " + memberId);
	}
	
	public static RentalAccessDeniedException sellerMismatch(Long sellerId, Long requestMemberId) {
		
		return new RentalAccessDeniedException("판매자 정보가 일치하지 않습니다. sellerId: " + sellerId + ", memberId: " + requestMemberId);
	}
}
