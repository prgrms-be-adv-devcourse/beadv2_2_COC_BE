package com.coc.modi.seller.seller.infrastructure.client.rental;

import org.springframework.stereotype.Component;

import com.coc.modi.common.ErrorCode;
import com.coc.modi.seller.seller.exception.SellerException;
import com.coc.modi.seller.seller.infrastructure.client.rental.dto.RentalListResponse;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RentalClientAdapter {
	
	private final RentalFeignClient rentalFeignClient;
	
	@Retry(name = "sellerRentalLookupRetry")
	@CircuitBreaker(name = "sellerRentalLookupCircuitBreaker", fallbackMethod = "fallbackGetRentals")
	public RentalListResponse getRentals(Long sellerId,
										 Long productId,
										 String status,
										 String startDate,
										 String endDate,
										 Integer page,
										 Integer size) {
		
		return rentalFeignClient.getRentals(sellerId, productId, status, startDate, endDate, page, size);
	}
	
	private RentalListResponse fallbackGetRentals(Long sellerId,
												  Long productId,
												  String status,
												  String startDate,
												  String endDate,
												  Integer page,
												  Integer size,
												  Throwable throwable) {
		
		log.warn("렌탈 서비스 조회 실패 sellerId={}, productId={}, status={}, period={}~{}", sellerId, productId, status, startDate,
				endDate, throwable);
		throw new SellerException(ErrorCode.INTERNAL_ERROR, "렌탈 서비스 호출에 실패했습니다.");
	}
}
