package com.coc.modi.delivery.exception;

import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.common.BaseException;
import com.coc.modi.common.ErrorCode;
import com.coc.modi.delivery.delivery.exception.DeliveryException;

@RestControllerAdvice(basePackages = "com.coc.modi.delivery")
public class DeliveryExceptionHandler {
	
	private static final Logger log = LoggerFactory.getLogger(DeliveryExceptionHandler.class);
	
	@ExceptionHandler(DeliveryException.class)
	public ResponseEntity<ApiResponse<?>> handleBaseException(DeliveryException ex) {
		
		log.warn("Product exception: code={}, message={}", ex.getErrorCode().getCode(), ex.getDetailMessage());
		return buildResponse(ex.getErrorCode(), ex.getDetailMessage());
	}
	
	@ExceptionHandler(BaseException.class)
	public ResponseEntity<ApiResponse<?>> handleBaseException(BaseException ex) {
		
		log.warn("Business exception: code={}, message={}", ex.getErrorCode().getCode(), ex.getDetailMessage());
		return buildResponse(ex.getErrorCode(), ex.getDetailMessage());
	}
	
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiResponse<?>> handleIllegalArgument(IllegalArgumentException ex) {
		
		log.warn("Illegal argument: {}", ex.getMessage());
		return buildResponse(ErrorCode.INVALID_INPUT, ex.getMessage());
	}
	
	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<ApiResponse<?>> handleIllegalState(IllegalStateException ex) {
		
		log.warn("Illegal state: {}", ex.getMessage());
		return buildResponse(ErrorCode.CONFLICT, ex.getMessage());
	}
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<?>> handleValidation(MethodArgumentNotValidException ex) {
		
		String message = Optional.of(ex.getBindingResult())
				.map(bindingResult -> bindingResult.getFieldErrors()
						.stream()
						.map(error -> error.getField() + ": " + error.getDefaultMessage())
						.collect(Collectors.joining(", ")))
				.filter(str -> !str.isBlank())
				.orElse(ErrorCode.INVALID_INPUT.getDefaultMessage());
		
		log.warn("Validation error: {}", message);
		return buildResponse(ErrorCode.INVALID_INPUT, message);
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<?>> handleUnexpected(Exception ex) {
		
		log.error("Unexpected error", ex);
		return buildResponse(ErrorCode.INTERNAL_ERROR, ErrorCode.INTERNAL_ERROR.getDefaultMessage());
	}
	
	private ResponseEntity<ApiResponse<?>> buildResponse(ErrorCode errorCode, String message) {
		
		return ResponseEntity
				.status(errorCode.getStatus())
				.body(ApiResponse.error(errorCode, message));
	}
}
