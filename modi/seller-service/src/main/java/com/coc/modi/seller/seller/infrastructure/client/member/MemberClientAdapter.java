package com.coc.modi.seller.seller.infrastructure.client.member;

import org.springframework.stereotype.Component;

import com.coc.modi.common.ErrorCode;
import com.coc.modi.seller.exception.SellerException;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberClientAdapter {
	
	private final MemberFeignClient memberFeignClient;
	
	@Retry(name = "memberRoleRetry")
	@CircuitBreaker(name = "memberRoleCircuitBreaker", fallbackMethod = "fallbackChangeMemberRole")
	public void changeMemberRole(Long memberId) {
		
		memberFeignClient.changeMemberRole(memberId);
	}
	
	private void fallbackChangeMemberRole(Long memberId, Throwable throwable) {
		
		log.warn("회원 역할 변경 요청 실패 memberId={}", memberId, throwable);
		throw new SellerException(ErrorCode.INTERNAL_ERROR, "회원 서비스 호출에 실패했습니다.");
	}
}
