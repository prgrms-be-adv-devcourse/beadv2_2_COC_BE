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
}
