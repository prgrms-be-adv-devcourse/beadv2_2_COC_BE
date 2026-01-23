package com.coc.modi.review.infrastructure.client;

import org.springframework.stereotype.Component;

import com.coc.modi.common.ErrorCode;
import com.coc.modi.review.exception.ReviewException;
import com.coc.modi.review.infrastructure.client.dto.RentalItemInfo;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RentalClientAdapter {
	
	private final RentalFeignClient rentalFeignClient;
	
	@Retry(name = "reviewRentalLookupRetry")
	@CircuitBreaker(name = "reviewRentalLookupCircuitBreaker", fallbackMethod = "fallbackGetRentalItem")
	public RentalItemInfo getRentalItem(Long rentalItemId) {
		
		return rentalFeignClient.getRentalItem(rentalItemId);
	}
	
	private RentalItemInfo fallbackGetRentalItem(Long rentalItemId, Throwable throwable) {
		
		log.warn("렌탈 아이템 조회 실패 rentalItemId={}", rentalItemId, throwable);
		throw new ReviewException(ErrorCode.INTERNAL_ERROR, "렌탈 서비스 호출에 실패했습니다.");
	}
}
