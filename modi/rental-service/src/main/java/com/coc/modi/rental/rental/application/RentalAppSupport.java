package com.coc.modi.rental.rental.application;

import org.springframework.stereotype.Component;

import com.coc.modi.rental.rental.domain.Rental;
import com.coc.modi.rental.rental.domain.RentalItem;
import com.coc.modi.rental.rental.domain.RentalItemRepository;
import com.coc.modi.rental.rental.exception.RentalAccessDeniedException;
import com.coc.modi.rental.rental.exception.RentalItemNotFoundException;
import com.coc.modi.rental.rental.exception.RentalNotFoundException;
import com.coc.modi.rental.rental.exception.RentalStatusInvalidException;
import com.coc.modi.rental.rental.infrastructure.client.SellerFeignClient;
import com.coc.modi.rental.rental.infrastructure.client.dto.SellerInfoResponse;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class RentalAppSupport {
	
	private final RentalItemRepository rentalItemRepository;
	private final SellerFeignClient sellerFeignClient;
	
	public RentalItem loadRentalItem(Long rentalItemId) {
		
		return rentalItemRepository.findById(rentalItemId)
				.orElseThrow(() -> new RentalItemNotFoundException(rentalItemId));
	}
	
	public Rental requireRental(RentalItem rentalItem) {
		
		Rental rental = rentalItem.getRental();
		
		if (rental == null) {
			
			throw new RentalNotFoundException(rentalItem.getId());
		}
		
		return rental;
	}
	
	public void requireMember(Rental rental, Long memberId) {
		
		if (!rental.getMemberId().equals(memberId)) {
			
			throw RentalAccessDeniedException.memberMismatch(rental.getId(), memberId);
		}
	}
	
	@Retry(name = "sellerVerifyRetry")
	@CircuitBreaker(name = "sellerVerifyCircuitBreaker", fallbackMethod = "fallbackRequireSeller")
	public void requireSeller(Long sellerId, Long memberId) {
		
		SellerInfoResponse sellerInfoResponse = sellerFeignClient.getSellerInfo(sellerId);
		if (sellerInfoResponse == null || sellerInfoResponse.memberId() == null) {
			throw new RentalStatusInvalidException("판매자 정보 조회에 실패했습니다.");
		}
		
		if (!sellerInfoResponse.memberId().equals(memberId)) {
			
			throw RentalAccessDeniedException.sellerMismatch(sellerId, memberId);
		}
	}
	
	private void fallbackRequireSeller(Long sellerId, Long memberId, Throwable throwable) {
		
		log.warn("판매자 검증 중 외부 호출 실패 sellerId={}, memberId={}", sellerId, memberId, throwable);
		throw new RentalStatusInvalidException("판매자 정보 조회에 실패했습니다.");
	}
}
