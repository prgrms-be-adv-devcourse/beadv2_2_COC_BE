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

	REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "REVIEW-404", "리뷰를 찾을 수 없습니다."),
	REVIEW_FORBIDDEN(HttpStatus.FORBIDDEN, "REVIEW-403", "리뷰 접근 권한이 없습니다.");
	
	private final HttpStatus status;
	private final String code;
	private final String defaultMessage;
	
	ErrorCode(HttpStatus status, String code, String defaultMessage) {
		
		this.status = status;
		this.code = code;
		this.defaultMessage = defaultMessage;
	}
	
}
