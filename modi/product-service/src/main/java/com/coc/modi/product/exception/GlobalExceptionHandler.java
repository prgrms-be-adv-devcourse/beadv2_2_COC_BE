package com.coc.modi.product.exception;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.common.BaseException;
import com.coc.modi.common.ErrorCode;
import com.coc.modi.product.product.exception.ProductAccessDeniedException;
import com.coc.modi.product.product.exception.ProductConflictException;
import com.coc.modi.product.product.exception.ProductException;
import com.coc.modi.product.product.exception.ProductInternalException;
import com.coc.modi.product.product.exception.ProductInvalidInputException;
import com.coc.modi.product.product.exception.ProductNotFoundException;
import com.coc.modi.product.product.exception.ProductSearchUnavailableException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;

import static net.logstash.logback.argument.StructuredArguments.kv;
@RestControllerAdvice(basePackages = "com.coc.modi.product")
public class GlobalExceptionHandler {
	
	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	
	@ExceptionHandler(ProductSearchUnavailableException.class)
	public ResponseEntity<ApiResponse<?>> handleBaseException(ProductSearchUnavailableException ex, HttpServletRequest request) {
		
		logBusinessException("product_search_unavailable", ex.getErrorCode(), ex.getDetailMessage(), request);
		return buildResponse(ex.getErrorCode(), ex.getDetailMessage());
	}
	
	@ExceptionHandler(ProductNotFoundException.class)
	public ResponseEntity<ApiResponse<?>> handleBaseException(ProductNotFoundException ex, HttpServletRequest request) {
		
		logBusinessException("product_not_found", ex.getErrorCode(), ex.getDetailMessage(), request);
		return buildResponse(ex.getErrorCode(), ex.getDetailMessage());
	}
	
	@ExceptionHandler(ProductInvalidInputException.class)
	public ResponseEntity<ApiResponse<?>> handleBaseException(ProductInvalidInputException ex, HttpServletRequest request) {
		
		logBusinessException("product_invalid_input", ex.getErrorCode(), ex.getDetailMessage(), request);
		return buildResponse(ex.getErrorCode(), ex.getDetailMessage());
	}
	
	@ExceptionHandler(ProductInternalException.class)
	public ResponseEntity<ApiResponse<?>> handleBaseException(ProductInternalException ex, HttpServletRequest request) {
		
		logBusinessException("product_internal_exception", ex.getErrorCode(), ex.getDetailMessage(), request);
		return buildResponse(ex.getErrorCode(), ex.getDetailMessage());
	}
	
	@ExceptionHandler(ProductConflictException.class)
	public ResponseEntity<ApiResponse<?>> handleBaseException(ProductConflictException ex, HttpServletRequest request) {
		
		logBusinessException("product_conflict", ex.getErrorCode(), ex.getDetailMessage(), request);
		return buildResponse(ex.getErrorCode(), ex.getDetailMessage());
	}
	
	@ExceptionHandler(ProductAccessDeniedException.class)
	public ResponseEntity<ApiResponse<?>> handleBaseException(ProductAccessDeniedException ex, HttpServletRequest request) {
		
		logBusinessException("product_access_denied", ex.getErrorCode(), ex.getDetailMessage(), request);
		return buildResponse(ex.getErrorCode(), ex.getDetailMessage());
	}
	
	@ExceptionHandler(ProductException.class)
	public ResponseEntity<ApiResponse<?>> handleBaseException(ProductException ex, HttpServletRequest request) {
		
		logBusinessException("product_exception", ex.getErrorCode(), ex.getDetailMessage(), request);
		return buildResponse(ex.getErrorCode(), ex.getDetailMessage());
	}
	
	@ExceptionHandler(BaseException.class)
	public ResponseEntity<ApiResponse<?>> handleBaseException(BaseException ex, HttpServletRequest request) {
		
		logBusinessException("business_exception", ex.getErrorCode(), ex.getDetailMessage(), request);
		return buildResponse(ex.getErrorCode(), ex.getDetailMessage());
	}
	
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiResponse<?>> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
		
		logBusinessException("illegal_argument", ErrorCode.INVALID_INPUT, ex.getMessage(), request);
		return buildResponse(ErrorCode.INVALID_INPUT, ex.getMessage());
	}
	
	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<ApiResponse<?>> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
		
		logBusinessException("illegal_state", ErrorCode.CONFLICT, ex.getMessage(), request);
		return buildResponse(ErrorCode.CONFLICT, ex.getMessage());
	}
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<?>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
		
		String message = Optional.of(ex.getBindingResult())
				.map(bindingResult -> bindingResult.getFieldErrors()
						.stream()
						.map(error -> error.getField() + ": " + error.getDefaultMessage())
						.collect(Collectors.joining(", ")))
				.filter(str -> !str.isBlank())
				.orElse(ErrorCode.INVALID_INPUT.getDefaultMessage());
		
		logBusinessException("validation_error", ErrorCode.INVALID_INPUT, message, request);
		return buildResponse(ErrorCode.INVALID_INPUT, message);
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<?>> handleUnexpected(Exception ex, HttpServletRequest request) {
		
		logUnexpectedException(ex, request);
		return buildResponse(ErrorCode.INTERNAL_ERROR, ErrorCode.INTERNAL_ERROR.getDefaultMessage());
	}

	private void logBusinessException(String eventName, ErrorCode errorCode, String message, HttpServletRequest request) {
		log.warn(eventName,
				kv("error.code", errorCode.getCode()),
				kv("error.message", message),
				kv("error.type", errorCode.name()),
				kv("http.status", errorCode.getStatus().value()),
				kv("request.path", request != null ? request.getRequestURI() : null));
	}

	private void logUnexpectedException(Exception ex, HttpServletRequest request) {
		ErrorCode errorCode = ErrorCode.INTERNAL_ERROR;
		log.error("unexpected_exception",
				kv("error.code", errorCode.getCode()),
				kv("error.message", ex.getMessage()),
				kv("error.type", errorCode.name()),
				kv("http.status", errorCode.getStatus().value()),
				kv("request.path", request != null ? request.getRequestURI() : null),
				kv("exception.class", ex.getClass().getName()),
				ex);
	}
	
	private ResponseEntity<ApiResponse<?>> buildResponse(ErrorCode errorCode, String message) {
		
		return ResponseEntity
				.status(errorCode.getStatus())
				.body(ApiResponse.error(errorCode, message));
	}
}
