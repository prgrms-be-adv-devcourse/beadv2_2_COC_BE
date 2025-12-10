package com.coc.modi.common;

public record ApiResponse<T>(boolean success,
							 String code,
							 String message,
							 T data) {

    public static <T> ApiResponse<T> ok(T data) {

        return new ApiResponse<>(true, "OK", "요청 성공", data);
    }

    public static ApiResponse<?> ok() {

        return new ApiResponse<>(true, "OK", "요청 성공", null);
    }

    public static ApiResponse<?> error(String code, String message) {

        return new ApiResponse<>(false, code, message, null);
    }
	
	public static ApiResponse<?> error(ErrorCode errorCode) {
		
		return new ApiResponse<>(false, errorCode.getCode(), errorCode.getDefaultMessage(), null);
	}
	
	public static ApiResponse<?> error(ErrorCode errorCode, String message) {
		
		String resolvedMessage = message == null || message.isBlank()
				? errorCode.getDefaultMessage()
				: message;
		
		return new ApiResponse<>(false, errorCode.getCode(), resolvedMessage, null);
	}
}
