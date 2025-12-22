package com.coc.modi.rental.rental.infrastructure.client;

import org.springframework.stereotype.Component;

import com.coc.modi.rental.rental.exception.RentalStatusInvalidException;
import com.coc.modi.rental.rental.infrastructure.client.dto.ChargeWalletCommand;
import com.coc.modi.rental.rental.infrastructure.client.dto.RefundWalletCommand;
import com.coc.modi.rental.rental.infrastructure.client.dto.WalletInfoResponse;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountClientAdapter {
	
	private final AccountFeignClient accountFeignClient;
	
	@Retry(name = "walletChargeRetry")
	@CircuitBreaker(name = "walletChargeCircuitBreaker", fallbackMethod = "fallbackCharge")
	public WalletInfoResponse charge(ChargeWalletCommand command) {
		
		return accountFeignClient.charge(command);
	}
	
	@Retry(name = "walletRefundRetry")
	@CircuitBreaker(name = "walletRefundCircuitBreaker", fallbackMethod = "fallbackRefund")
	public void refund(RefundWalletCommand command) {
		
		accountFeignClient.refund(command);
	}
	
	private WalletInfoResponse fallbackCharge(ChargeWalletCommand command, Throwable throwable) {
		
		log.warn("지갑 차지 호출 실패, circuit breaker 동작 rentalId={}, memberId={}, amount={}",
				command.rentalId(), command.memberId(), command.amount(), throwable);
		throw new RentalStatusInvalidException("지갑 서비스 호출에 실패했습니다.");
	}
	
	private void fallbackRefund(RefundWalletCommand command, Throwable throwable) {
		
		log.warn("지갑 환불 호출 실패, circuit breaker 동작 rentalId={}, memberId={}, rentalItemId={}, amount={}",
				command.rentalId(), command.memberId(), command.rentalItemId(), command.amount(), throwable);
		throw new RentalStatusInvalidException("환불 처리 중 지갑 서비스 호출에 실패했습니다.");
	}
}
