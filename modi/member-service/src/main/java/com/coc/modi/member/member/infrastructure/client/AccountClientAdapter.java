package com.coc.modi.member.member.infrastructure.client;

import org.springframework.stereotype.Component;

import com.coc.modi.member.member.infrastructure.client.dto.MemberWalletResponse;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountClientAdapter {
	
	private final AccountFeignClient accountFeignClient;
	
	@Retry(name = "walletGetBalanceRetry")
	@CircuitBreaker(name = "walletGetBalanceCircuitBreaker", fallbackMethod = "fallbackGetBalance")
	public MemberWalletResponse getWalletBalance(Long memberId) {
		
		return accountFeignClient.getWalletBalance(memberId);
	}
}
