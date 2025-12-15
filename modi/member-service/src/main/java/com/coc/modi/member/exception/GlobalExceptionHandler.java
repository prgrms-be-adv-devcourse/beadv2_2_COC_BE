package com.coc.modi.member.exception;

import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.common.BaseException;
import com.coc.modi.common.ErrorCode;
import com.coc.modi.member.member.exception.MemberException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice(basePackages = "com.coc.modi.member")
public class GlobalExceptionHandler {
	
	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	
	@ExceptionHandler(MemberException.class)
	public ResponseEntity<ApiResponse<?>> handleMemberException(MemberException e) {
		
		ErrorCode errorCode = e.getErrorCode();
		String message = e.getDetailMessage();
		
		log.warn("Member exception: code={}, message={}", errorCode.getCode(), message);
		
		return ResponseEntity
				.status(errorCode.getStatus())
				.body(ApiResponse.error(errorCode, message));
	}
	
	@ExceptionHandler(BaseException.class)
	public ResponseEntity<ApiResponse<?>> handleBaseException(BaseException ex) {
		
		ErrorCode errorCode = ex.getErrorCode();
		String message = ex.getDetailMessage();
		
		log.warn("Business exception: code={}, message={}", errorCode.getCode(), message);
		
		return ResponseEntity
				.status(errorCode.getStatus())
				.body(ApiResponse.error(errorCode, message));
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
		
		String message = Optional.ofNullable(ex.getBindingResult())
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

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiResponse<?>> handleDataIntegrity(DataIntegrityViolationException ex,
			HttpServletRequest request) {

		if (request.getRequestURI().contains("/api/members/signup")) {
			log.warn("Duplicate email violation: {}", ex.getMessage());
			return buildResponse(ErrorCode.EMAIL_DUPLICATED, ErrorCode.EMAIL_DUPLICATED.getDefaultMessage());
		}

		log.error("Data integrity violation", ex);
		return buildResponse(ErrorCode.CONFLICT, ErrorCode.CONFLICT.getDefaultMessage());
	}
	
	private ResponseEntity<ApiResponse<?>> buildResponse(ErrorCode errorCode, String message) {
		
		HttpStatus status = errorCode.getStatus();
		
		return ResponseEntity
				.status(status)
				.body(ApiResponse.error(errorCode, message));
	}
}
