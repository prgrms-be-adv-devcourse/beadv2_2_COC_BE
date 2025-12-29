package com.coc.modi.common;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum ErrorCode {
	
	INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON-400", "잘못된 요청입니다."),
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON-401", "인증이 필요합니다."),
	FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON-403", "권한이 없습니다."),
	NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON-404", "대상을 찾을 수 없습니다."),
	CONFLICT(HttpStatus.CONFLICT, "COMMON-409", "요청이 현재 상태와 충돌합니다."),
	INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-500", "서버 오류가 발생했습니다."),
	
	RENTAL_NOT_FOUND(HttpStatus.NOT_FOUND, "RENTAL-404", "렌탈 정보를 찾을 수 없습니다."),
	RENTAL_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "RENTAL-ITEM-404", "대여 아이템 정보를 찾을 수 없습니다."),
	RENTAL_STATUS_INVALID(HttpStatus.CONFLICT, "RENTAL-409", "허용되지 않은 대여 상태 변경입니다."),
	RENTAL_MEMBER_MISMATCH(HttpStatus.FORBIDDEN, "RENTAL-403", "대여 요청자와 요청 멤버가 일치하지 않습니다."),
	RENTAL_SELLER_MISMATCH(HttpStatus.FORBIDDEN, "RENTAL-SELLER-403", "판매자 정보가 일치하지 않습니다."),

	PRODUCT_INVALID_INPUT(HttpStatus.BAD_REQUEST, "PRODUCT-400", "잘못된 상품 요청입니다."),
	PRODUCT_FORBIDDEN(HttpStatus.FORBIDDEN, "PRODUCT-403", "상품 접근 권한이 없습니다."),
	PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT-404", "상품을 찾을 수 없습니다."),
	PRODUCT_CONFLICT(HttpStatus.CONFLICT, "PRODUCT-409", "상품 상태가 요청과 충돌합니다."),
	PRODUCT_INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "PRODUCT-500", "상품 처리 중 오류가 발생했습니다."),
	PRODUCT_SEARCH_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "PRODUCT-503", "상품 검색 기능을 이용할 수 없습니다."),

	DELIVERY_INVALID_INPUT(HttpStatus.BAD_REQUEST, "DELIVERY-400", "잘못된 배송 요청입니다."),
	DELIVERY_NOT_FOUND(HttpStatus.NOT_FOUND, "DELIVERY-404", "배송 정보를 찾을 수 없습니다."),
	DELIVERY_CONFLICT(HttpStatus.CONFLICT, "DELIVERY-409", "배송 요청이 현재 상태와 충돌합니다."),
	DELIVERY_TRACKING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "DELIVERY-500", "배송 추적 처리 중 오류가 발생했습니다."),

	REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "REVIEW-404", "리뷰를 찾을 수 없습니다."),
	REVIEW_FORBIDDEN(HttpStatus.FORBIDDEN, "REVIEW-403", "리뷰 접근 권한이 없습니다."),
	
	MEMBER_WITHDRAWN(HttpStatus.BAD_REQUEST, "MEMBER-WITHDRAWN", "탈퇴한 회원입니다."),
	MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER-404", "회원 정보를 찾을 수 없습니다."),
	MEMER_ACCESS_DENIED(HttpStatus.FORBIDDEN, "MEMBER-403", "사용 불가능한 회원입니다."),
	EMAIL_DUPLICATED(HttpStatus.CONFLICT, "MEMBER-EMAIL-409", "이미 사용 중인 이메일입니다."),
	PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "MEMBER-PASSWORD-400", "비밀번호가 일치하지 않습니다."),
	NAME_MISMATCH(HttpStatus.BAD_REQUEST, "MEMBER-NAME-400", "이름이 일치하지 않습니다."),
	EMAIL_MISMATCH(HttpStatus.BAD_REQUEST, "MEMBER-EMAIL-400", "이메일이 일치하지 않습니다."),
	ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER-ADDRESS-404", "주소 정보를 찾을 수 없습니다."),
	AUTH_CODE_INVALID(HttpStatus.BAD_REQUEST, "MEMBER-AUTH-400", "이메일 인증 코드가 유효하지 않습니다."),
	MEMBER_ROLE_INVALID(HttpStatus.BAD_REQUEST, "MEMBER-ROLE-400", "요청한 역할 상태가 일치하지 않습니다."),
	PHONE_DUPLICATED(HttpStatus.CONFLICT, "MEMBER-PHONE-409", "이미 사용 중인 휴대폰 번호입니다."),
	
	ACCOUNT_BALENCE_REMAIN(HttpStatus.BAD_REQUEST, "ACCOUNT-BALANCE-REMAIN-400", "지갑에 잔액이 남아있습니다."),
	ACCOUNT_BALENCE_CHECK(HttpStatus.NOT_FOUND, "ACCOUNT-BALANCE-404", "지갑 잔액 체크에 실패했습니다"),
	ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "ACCOUNT-404", "지갑 정보를 찾을 수 없습니다."),
	ACCOUNT_BALANCE_INSUFFICIENT(HttpStatus.BAD_REQUEST, "ACCOUNT-BALANCE-400", "잔액이 부족합니다."),
	ACCOUNT_ALREADY_EXISTS(HttpStatus.CONFLICT, "ACCOUNT-409", "지갑이 이미존재합니다."),
	ACCOUNT_TRANSACTION_NOT_FOUND(HttpStatus.NOT_FOUND, "ACCOUNT-TXN-404", "거래 정보를 찾을 수 없습니다."),
	ACCOUNT_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "ACCOUNT-503", "지갑 서비스를 사용할 수 없습니다."),
	
	SELLER_NOT_FOUND(HttpStatus.NOT_FOUND, "SELLER-404", "판매자를 찾을 수 없습니다."),
	SELLER_DUPLICATE(HttpStatus.CONFLICT, "SELLER-409", "이미 등록된 판매자입니다."),
	SETTLEMENT_BATCH_NOT_FOUND(HttpStatus.NOT_FOUND, "SETTLEMENT-BATCH-404", "정산 배치를 찾을 수 없습니다."),
	SETTLEMENT_BATCH_DUPLICATE(HttpStatus.CONFLICT, "SETTLEMENT-BATCH-409", "이미 생성된 정산 배치입니다."),
	SETTLEMENT_BATCH_EXECUTION_NOT_FOUND(HttpStatus.NOT_FOUND, "SETTLEMENT-BATCH-EXEC-404", "배치 실행을 찾을 수 없습니다."),
	SELLER_SETTLEMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "SELLER-SETTLEMENT-404", "정산서를 찾을 수 없습니다."),
	SELLER_SETTLEMENT_CONFLICT(HttpStatus.CONFLICT, "SELLER-SETTLEMENT-409", "정산서가 이미 다른 배치로 처리되었습니다."),
	SELLER_SETTLEMENT_FORBIDDEN(HttpStatus.FORBIDDEN, "SELLER-SETTLEMENT-403", "정산서 소유자가 일치하지 않습니다.");
	
	private final HttpStatus status;
	private final String code;
	private final String defaultMessage;
	
	ErrorCode(HttpStatus status, String code, String defaultMessage) {
		
		this.status = status;
		this.code = code;
		this.defaultMessage = defaultMessage;
	}
	
}
