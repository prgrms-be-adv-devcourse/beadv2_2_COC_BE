package com.coc.modi.rental.rental.infrastructure.client;

import java.util.List;

import org.springframework.stereotype.Component;

import com.coc.modi.common.ErrorCode;
import com.coc.modi.rental.rental.exception.RentalException;
import com.coc.modi.rental.rental.infrastructure.client.dto.ProductResponseDto;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductClientAdapter {
	
	private final ProductFeignClient productFeignClient;
	
	@Retry(name = "productLookupRetry")
	@CircuitBreaker(name = "productLookupCircuitBreaker", fallbackMethod = "fallbackGetProducts")
	public List<ProductResponseDto> getProducts(List<Long> productIds) {
		
		return productFeignClient.getProducts(productIds);
	}
	
	@Retry(name = "productLookupRetry")
	@CircuitBreaker(name = "productLookupCircuitBreaker", fallbackMethod = "fallbackGetProduct")
	public ProductResponseDto getProduct(Long productId) {
		
		List<ProductResponseDto> products = productFeignClient.getProducts(List.of(productId));
		
		if (products.isEmpty()) {
			
			throw new RentalException(ErrorCode.NOT_FOUND, "상품이 존재하지 않습니다. productId: " + productId);
		}
		
		return products.get(0);
	}
	
	private List<ProductResponseDto> fallbackGetProducts(List<Long> productIds, Throwable throwable) {
		
		log.warn("상품 서비스 조회 실패, circuit breaker 동작 productIds={}", productIds, throwable);
		throw new RentalException(ErrorCode.PRODUCT_INTERNAL_ERROR, "상품 서비스 호출에 실패했습니다.");
	}
	
	private ProductResponseDto fallbackGetProduct(Long productId, Throwable throwable) {
		
		log.warn("상품 서비스 단건 조회 실패, circuit breaker 동작 productId={}", productId, throwable);
		throw new RentalException(ErrorCode.PRODUCT_INTERNAL_ERROR, "상품 서비스 호출에 실패했습니다.");
	}
}
