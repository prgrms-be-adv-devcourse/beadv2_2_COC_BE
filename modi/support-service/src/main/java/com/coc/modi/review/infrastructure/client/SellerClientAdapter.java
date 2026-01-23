package com.coc.modi.review.infrastructure.client;

import org.springframework.stereotype.Component;

import com.coc.modi.common.ErrorCode;
import com.coc.modi.review.exception.ReviewException;
import com.coc.modi.review.infrastructure.client.dto.SellerInfoResponse;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SellerClientAdapter {

	private final SellerFeignClient sellerFeignClient;

	@Retry(name = "reviewSellerLookupRetry")
	@CircuitBreaker(name = "reviewSellerLookupCircuitBreaker", fallbackMethod = "fallbackGetSellerMemberId")
	public Long getSellerMemberId(Long sellerId) {

		SellerInfoResponse response = sellerFeignClient.getSellerInfo(sellerId);
		if (response == null || response.memberId() == null) {
			throw new ReviewException(ErrorCode.SELLER_NOT_FOUND, "판매자 정보를 찾을 수 없습니다.");
		}

		return response.memberId();
	}

	private Long fallbackGetSellerMemberId(Long sellerId, Throwable throwable) {

		log.warn("판매자 조회 실패 sellerId={}", sellerId, throwable);
		throw new ReviewException(ErrorCode.INTERNAL_ERROR, "판매자 서비스 호출에 실패했습니다.");
	}
}
