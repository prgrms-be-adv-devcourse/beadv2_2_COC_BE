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

@RestControllerAdvice(basePackages = "com.coc.modi.product")
public class GlobalExceptionHandler {
	
	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	
	@ExceptionHandler(ProductSearchUnavailableException.class)
	public ResponseEntity<ApiResponse<?>> handleBaseException(ProductSearchUnavailableException ex) {
		
		log.warn("Product search unavailable exception: code={}, message={}", ex.getErrorCode().getCode(), ex.getDetailMessage());
		return buildResponse(ex.getErrorCode(), ex.getDetailMessage());
	}
	
	@ExceptionHandler(ProductNotFoundException.class)
	public ResponseEntity<ApiResponse<?>> handleBaseException(ProductNotFoundException ex) {
		
		log.warn("Product not found exception: code={}, message={}", ex.getErrorCode().getCode(), ex.getDetailMessage());
		return buildResponse(ex.getErrorCode(), ex.getDetailMessage());
	}
	
	@ExceptionHandler(ProductInvalidInputException.class)
	public ResponseEntity<ApiResponse<?>> handleBaseException(ProductInvalidInputException ex) {
		
		log.warn("Product invalid exception: code={}, message={}", ex.getErrorCode().getCode(), ex.getDetailMessage());
		return buildResponse(ex.getErrorCode(), ex.getDetailMessage());
	}
	
	@ExceptionHandler(ProductInternalException.class)
	public ResponseEntity<ApiResponse<?>> handleBaseException(ProductInternalException ex) {
		
		log.warn("Product internal exception: code={}, message={}", ex.getErrorCode().getCode(), ex.getDetailMessage());
		return buildResponse(ex.getErrorCode(), ex.getDetailMessage());
	}
	
	@ExceptionHandler(ProductConflictException.class)
	public ResponseEntity<ApiResponse<?>> handleBaseException(ProductConflictException ex) {
		
		log.warn("Product conflict exception: code={}, message={}", ex.getErrorCode().getCode(), ex.getDetailMessage());
		return buildResponse(ex.getErrorCode(), ex.getDetailMessage());
	}
	
	@ExceptionHandler(ProductAccessDeniedException.class)
	public ResponseEntity<ApiResponse<?>> handleBaseException(ProductAccessDeniedException ex) {
		
		log.warn("Product access denied exception: code={}, message={}", ex.getErrorCode().getCode(), ex.getDetailMessage());
		return buildResponse(ex.getErrorCode(), ex.getDetailMessage());
	}
	
	@ExceptionHandler(ProductException.class)
	public ResponseEntity<ApiResponse<?>> handleBaseException(ProductException ex) {
		
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
