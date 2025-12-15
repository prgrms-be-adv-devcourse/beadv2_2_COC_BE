package com.coc.modi.rental.rental.application;

import com.coc.modi.rental.rental.domain.Rental;
import com.coc.modi.rental.rental.domain.RentalItem;
import com.coc.modi.rental.rental.domain.RentalItemStatus;
import com.coc.modi.rental.rental.domain.RentalStatus;
import com.coc.modi.rental.rental.exception.RentalStatusInvalidException;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RentalDecisionService {
	
	private final RentalAppSupport rentalAppSupport;
	
	@Transactional
	public void acceptRentalItem(Long rentalItemId, Long memberId) {
		
		decideRentalItem(rentalItemId, memberId, RentalItemStatus.ACCEPTED);
	}
	
	@Transactional
	public void rejectRentalItem(Long rentalItemId, Long memberId) {
		
		decideRentalItem(rentalItemId, memberId, RentalItemStatus.REJECTED);
	}
	
	private void decideRentalItem(Long rentalItemId, Long memberId, RentalItemStatus targetStatus) {
		
		RentalItem rentalItem = rentalAppSupport.loadRentalItem(rentalItemId);
		
		rentalAppSupport.requireSeller(rentalItem.getSellerId(), memberId);
		
		Rental rental = rentalAppSupport.requireRental(rentalItem);
		
		RentalStatus rentalStatus = rentalItem.getRental().getStatus();
		
		if (rentalStatus == RentalStatus.PAID
				|| rentalStatus == RentalStatus.IN_PROGRESS
				|| rentalStatus == RentalStatus.COMPLETED
				|| rentalStatus == RentalStatus.CANCELED) {
			
			throw new RentalStatusInvalidException("현재 대여 상태에서 승인 또는 거절이 불가능 합니다. rentalStatus=" + rentalStatus);
		}
		
		rentalItem.decide(targetStatus);
		rental.recalculateAmountsAndStatus();
	}
	
}
