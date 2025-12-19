package com.coc.modi.product.product.infrastructure.client;

import org.springframework.stereotype.Component;

import com.coc.modi.product.product.application.dto.RentalResponse;
import com.coc.modi.product.product.presentation.dto.RentalRequest;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RentalAvailabilityClient {
	
	private final RentalFeignClient rentalFeignClient;
	
	@Retry(name = "rentalAvailabilityRetry")
	@CircuitBreaker(name = "rentalAvailabilityCircuitBreaker", fallbackMethod = "fallbackUnavailableProducts")
	public RentalResponse unavailableProducts(RentalRequest rentalRequest) {
		
		return rentalFeignClient.unavailableProducts(rentalRequest);
	}
	
	private RentalResponse fallbackUnavailableProducts(RentalRequest rentalRequest, Throwable throwable) {
		
		log.warn("렌탈 서비스로부터 예약 불가 상품 조회 실패 request={}", rentalRequest, throwable);
		return new RentalResponse(java.util.List.of());
	}
}
