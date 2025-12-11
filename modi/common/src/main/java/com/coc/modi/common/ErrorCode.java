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
	
	MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER-404", "회원 정보를 찾을 수 없습니다."),
	EMAIL_DUPLICATED(HttpStatus.CONFLICT, "MEMBER-EMAIL-409", "이미 사용 중인 이메일입니다."),
	NICKNAME_DUPLICATED(HttpStatus.CONFLICT, "MEMBER-NICKNAME-409", "이미 사용 중인 닉네임입니다."),
	PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "MEMBER-PASSWORD-400", "비밀번호가 일치하지 않습니다."),
	ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER-ADDRESS-404", "주소 정보를 찾을 수 없습니다."),
	ADDRESS_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "MEMBER-ADDRESS-400", "등록 가능한 주소 수를 초과했습니다."),
	AUTH_CODE_INVALID(HttpStatus.BAD_REQUEST, "MEMBER-AUTH-400", "이메일 인증 코드가 유효하지 않습니다.");
	
	private final HttpStatus status;
	private final String code;
	private final String defaultMessage;
	
	ErrorCode(HttpStatus status, String code, String defaultMessage) {
		
		this.status = status;
		this.code = code;
		this.defaultMessage = defaultMessage;
	}
	
}
