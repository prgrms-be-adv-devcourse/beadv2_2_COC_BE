package com.coc.modi.product.product.application.support;

import org.springframework.stereotype.Component;

import com.coc.modi.product.product.application.dto.SellerDetailResponse;
import com.coc.modi.product.product.exception.ProductInvalidInputException;
import com.coc.modi.product.product.infrastructure.client.SellerFeignClient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class SellerMemberResolver {

	private final SellerFeignClient sellerFeignClient;

	@Retry(name = "sellerLookupRetry")
	@CircuitBreaker(name = "sellerLookupCircuitBreaker", fallbackMethod = "fallbackGetMemberId")
	public Long getMemberId(Long sellerId) {

		try {
			SellerDetailResponse response = sellerFeignClient.getSellerById(sellerId);

			if (response == null || response.memberId() == null) {
				throw new ProductInvalidInputException("판매자 정보를 찾을 수 없습니다. sellerId: " + sellerId);
			}

			return response.memberId();

		} catch (feign.FeignException.NotFound e) {

			throw new ProductInvalidInputException("판매자 정보가 등록되지 않았습니다. sellerId: " + sellerId);

		} catch (feign.FeignException e) {

			throw new ProductInvalidInputException("판매자 서비스 호출 중 오류가 발생했습니다.");
		}
	}

	private Long fallbackGetMemberId(Long sellerId, Throwable throwable) {

		log.warn("판매자 조회 실패, circuit breaker 동작 sellerId={}", sellerId, throwable);
		throw new ProductInvalidInputException("판매자 서비스 호출 중 오류가 발생했습니다.");
	}
}
