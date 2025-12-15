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

import feign.FeignException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
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
	
	public void requireSeller(Long sellerId, Long memberId) {
		
		try {
			SellerInfoResponse sellerInfoResponse = sellerFeignClient.getSellerInfo(sellerId);
			
			if (!sellerInfoResponse.memberId().equals(memberId)) {
				
				throw RentalAccessDeniedException.sellerMismatch(sellerId, memberId);
			}
		} catch (FeignException ex) {
			
			throw new RentalStatusInvalidException("판매자 정보 조회에 실패했습니다.");
		}
	}
}
