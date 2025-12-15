package com.coc.modi.rental.rental.domain;

public enum RentalItemStatus {
	
	REQUESTED,  // 판매자가 아직 수락 안 함
	ACCEPTED,   // 판매자 수락
	REJECTED,   // 판매자 거절
	RENTING,    // 대여 중
	RETURNED,   // 반납 완료
	CANCELED;    // 사용자가 해당 아이템만 취소
	
	public boolean isRenting() {
		
		return this == RENTING;
	}
}
