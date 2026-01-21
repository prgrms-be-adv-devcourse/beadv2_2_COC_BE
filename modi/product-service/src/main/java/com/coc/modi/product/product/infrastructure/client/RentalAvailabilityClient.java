package com.coc.modi.product.product.infrastructure.client;

import org.springframework.stereotype.Component;

import com.coc.modi.product.product.application.dto.RentalResponse;
import com.coc.modi.product.product.presentation.dto.RentalRequest;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static net.logstash.logback.argument.StructuredArguments.kv;

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
		
		log.warn("rental_unavailable_products_fallback",
				kv("rental.request", rentalRequest),
				kv("exception.class", throwable.getClass().getName()),
				throwable);
		return new RentalResponse(java.util.List.of());
	}
}
