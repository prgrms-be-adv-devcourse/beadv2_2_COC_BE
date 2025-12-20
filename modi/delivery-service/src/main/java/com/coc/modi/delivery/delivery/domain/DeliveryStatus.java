package com.coc.modi.delivery.delivery.domain;

public enum DeliveryStatus {
	
	REGISTERED,          // 송장 등록
	PICKED_UP,           // 집하
	IN_TRANSIT,          // 이동중
	OUT_FOR_DELIVERY,    // 배송출발
	DELIVERED,           // 배송 완료
	EXCEPTION,           // 예외(주소불명/분실/반송 등)
	CANCELLED            // 취소/배송 중단
}
