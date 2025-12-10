package com.coc.modi.rental.rental.application;

import com.coc.modi.rental.rental.domain.Rental;
import com.coc.modi.rental.rental.domain.RentalItem;
import com.coc.modi.rental.rental.domain.RentalItemStatus;
import com.coc.modi.rental.rental.domain.RentalItemRepository;
import com.coc.modi.rental.rental.domain.RentalStatus;
import com.coc.modi.rental.rental.exception.RentalAccessDeniedException;
import com.coc.modi.rental.rental.exception.RentalItemNotFoundException;
import com.coc.modi.rental.rental.exception.RentalNotFoundException;
import com.coc.modi.rental.rental.exception.RentalStatusInvalidException;
import com.coc.modi.rental.rental.infrastructure.client.SellerFeignClient;
import com.coc.modi.rental.rental.infrastructure.client.dto.SellerInfoResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RentalDecisionService {
	
	private final RentalItemRepository rentalItemRepository;
	private final SellerFeignClient sellerFeignClient;
	
	@Transactional
	public void acceptRentalItem(Long rentalItemId, Long memberId) {
		
		decideRentalItem(rentalItemId, memberId, RentalItemStatus.ACCEPTED);
	}
	
	@Transactional
	public void rejectRentalItem(Long rentalItemId, Long memberId) {
		
		decideRentalItem(rentalItemId, memberId, RentalItemStatus.REJECTED);
	}
	
	private void decideRentalItem(Long rentalItemId, Long memberId, RentalItemStatus targetStatus) {
		
		RentalItem rentalItem = rentalItemRepository.findById(rentalItemId)
				.orElseThrow(() -> new RentalItemNotFoundException(rentalItemId));
		
		Rental rental = rentalItem.getRental();
		
		SellerInfoResponse sellerInfoResponse = sellerFeignClient.getSellerInfo(rentalItem.getSellerId());
		
		if (!sellerInfoResponse.memberId().equals(memberId)) {
			
			throw RentalAccessDeniedException.sellerMismatch(rentalItem.getSellerId(), memberId);
		}
		
		if (rental == null) {
			
			throw new RentalNotFoundException(rentalItemId);
		}
		
		RentalStatus rentalStatus = rental.getStatus();
		
		if (rentalStatus == RentalStatus.PAID || rentalStatus == RentalStatus.IN_PROGRESS
				|| rentalStatus == RentalStatus.COMPLETED || rentalStatus == RentalStatus.CANCELED) {
			
			throw new RentalStatusInvalidException("현재 대여 정보의 상태에서 승인/거절이 불가능합니다. rentalStatus: " + rentalStatus);
		}
		
		rentalItem.decide(targetStatus);
		rental.updateStatusFromItems();
	}
	
}
