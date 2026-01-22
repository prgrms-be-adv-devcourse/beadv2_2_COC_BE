package com.coc.modi.notification.infrastructure.client.member;

import org.springframework.stereotype.Component;

import com.coc.modi.notification.infrastructure.client.member.dto.MemberEmailResponse;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberClientAdapter {

	private final MemberFeignClient memberFeignClient;

	@Retry(name = "memberEmailRetry")
	@CircuitBreaker(name = "memberEmailCircuitBreaker", fallbackMethod = "fallbackGetMemberEmail")
	public String getMemberEmail(Long memberId) {

		MemberEmailResponse response = memberFeignClient.getMemberEmail(memberId);
		return response == null ? null : response.email();
	}

	private String fallbackGetMemberEmail(Long memberId, Throwable throwable) {

		log.warn("회원 이메일 조회 실패 memberId={}", memberId, throwable);
		return null;
	}
}
