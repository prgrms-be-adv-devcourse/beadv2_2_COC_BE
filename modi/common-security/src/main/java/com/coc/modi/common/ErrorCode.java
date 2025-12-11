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

    SELLER_NOT_FOUND(HttpStatus.NOT_FOUND, "SELLER-404", "판매자를 찾을 수 없습니다."),
    SELLER_DUPLICATE(HttpStatus.CONFLICT, "SELLER-409", "이미 등록된 판매자입니다."),
    SETTLEMENT_BATCH_NOT_FOUND(HttpStatus.NOT_FOUND, "SETTLEMENT-BATCH-404", "정산 배치를 찾을 수 없습니다."),
    SETTLEMENT_BATCH_DUPLICATE(HttpStatus.CONFLICT, "SETTLEMENT-BATCH-409", "이미 생성된 정산 배치입니다."),
    SETTLEMENT_BATCH_EXECUTION_NOT_FOUND(HttpStatus.NOT_FOUND, "SETTLEMENT-BATCH-EXEC-404", "배치 실행을 찾을 수 없습니다."),
    SELLER_SETTLEMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "SELLER-SETTLEMENT-404", "정산서를 찾을 수 없습니다."),
    SELLER_SETTLEMENT_FORBIDDEN(HttpStatus.FORBIDDEN, "SELLER-SETTLEMENT-403", "정산서 소유자가 일치하지 않습니다."),

    RENTAL_NOT_FOUND(HttpStatus.NOT_FOUND, "RENTAL-404", "렌탈 정보를 찾을 수 없습니다."),
    RENTAL_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "RENTAL-ITEM-404", "대여 아이템 정보를 찾을 수 없습니다."),
    RENTAL_STATUS_INVALID(HttpStatus.CONFLICT, "RENTAL-409", "허용되지 않은 대여 상태 변경입니다."),
    RENTAL_MEMBER_MISMATCH(HttpStatus.FORBIDDEN, "RENTAL-403", "대여 요청자와 요청 멤버가 일치하지 않습니다."),
    RENTAL_SELLER_MISMATCH(HttpStatus.FORBIDDEN, "RENTAL-SELLER-403", "판매자 정보가 일치하지 않습니다.");

    private final HttpStatus status;
    private final String code;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String code, String defaultMessage) {
        this.status = status;
        this.code = code;
        this.defaultMessage = defaultMessage;
    }
}
