package com.coc.modi.rental.rental.application;

import com.coc.modi.rental.rental.application.dto.RentalReturnCommand;
import com.coc.modi.rental.rental.application.dto.RentalReturnResponse;
import com.coc.modi.rental.rental.application.dto.ExtendRentalCommand;
import com.coc.modi.rental.rental.domain.Rental;
import com.coc.modi.rental.rental.domain.RentalEventType;
import com.coc.modi.rental.rental.domain.RentalItem;
import com.coc.modi.rental.rental.domain.RentalItemStatus;
import com.coc.modi.rental.rental.domain.RentalItemRepository;
import com.coc.modi.rental.rental.domain.RentalRepository;
import com.coc.modi.rental.rental.infrastructure.client.AccountFeignClient;
import com.coc.modi.rental.rental.infrastructure.client.SellerFeignClient;
import com.coc.modi.rental.rental.infrastructure.client.dto.ChargeWalletCommand;
import com.coc.modi.rental.rental.infrastructure.client.dto.RefundWalletCommand;
import com.coc.modi.rental.rental.infrastructure.client.dto.SellerInfoResponse;
import com.coc.modi.rental.rental.exception.RentalAccessDeniedException;
import com.coc.modi.rental.rental.exception.RentalItemNotFoundException;
import com.coc.modi.rental.rental.exception.RentalNotFoundException;
import com.coc.modi.rental.rental.exception.RentalStatusInvalidException;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RentalLifecycleService {
	
	private final RentalRepository rentalRepository;
	private final RentalItemRepository rentalItemRepository;
	private final SellerFeignClient sellerFeignClient;
	private final AccountFeignClient accountFeignClient;
	private final RentalEventLogService rentalEventLogService;
	
	@Transactional
	public void cancelRentalItem(Long rentalItemId, Long memberId) {
		
		RentalItem rentalItem = rentalItemRepository.findById(rentalItemId)
				.orElseThrow(() -> new RentalItemNotFoundException(rentalItemId));
		
		if (!rentalItem.getRental().getMemberId().equals(memberId)) {
			
			throw RentalAccessDeniedException.memberMismatch(rentalItem.getRental().getId(), memberId);
		}
		
		if (re)
		
		rentalItem.cancelByMemberRequest();
		
		Rental rental = rentalItem.getRental();
		rental.updateStatusFromItems();
		
		rentalEventLogService.logEvent(rentalItem.getRental(), RentalEventType.RENTAL_CANCELED, Map.of("rentalItemId", rentalItemId, "status", rentalItem.getStatus()
				.name()));
	}
	
	@Transactional
	public RentalReturnResponse completeReturn(RentalReturnCommand command) {
		
		RentalItem rentalItem = rentalItemRepository.findById(command.rentalItemId())
				.orElseThrow(() -> new RentalItemNotFoundException(command.rentalItemId()));
		
		SellerInfoResponse sellerInfoResponse = sellerFeignClient.getSellerInfo(rentalItem.getSellerId());
		
		if (!sellerInfoResponse.memberId().equals(command.memberId())) {
			
			throw RentalAccessDeniedException.sellerMismatch(rentalItem.getSellerId(), command.memberId());
		}
		
		Rental rental = rentalItem.getRental();
		
		if (rental == null) {
			
			throw new RentalNotFoundException(command.rentalItemId());
		}
		
		rentalItem.processReturn();
		rental.updateStatusFromItems();
		
		BigDecimal damageFee = command.damageFee() == null ? BigDecimal.ZERO : command.damageFee();
		BigDecimal lateFee = command.lateFee() == null ? BigDecimal.ZERO : command.lateFee();
		
		rentalEventLogService.logEvent(rental, RentalEventType.RENTAL_RETURNED, Map.of("rentalId", rental.getId(), "rentalItemId", rentalItem.getId(), "rentalStatus", rental.getStatus()
				.name(), "itemStatus", rentalItem.getStatus()
				.name(), "damageFee", damageFee, "lateFee", lateFee, "extraFeeTotal", damageFee.add(lateFee), "damageReason", command.damageReason(), "lateReason", command.lateReason(), "memo", command.memo()));
		
		return new RentalReturnResponse(rental.getId(), rentalItem.getId(), rental.getStatus()
				.name(), damageFee.add(lateFee).toPlainString());
	}
	
	@Transactional
	public void extendRentalItem(ExtendRentalCommand command) {
		
		RentalItem rentalItem = rentalItemRepository.findById(command.rentalItemId())
				.orElseThrow(() -> new RentalItemNotFoundException(command.rentalItemId()));
		Rental rental = rentalItem.getRental();
		
		if (rental == null) {
			
			throw new RentalNotFoundException(command.rentalItemId());
		}
		
		if (!rental.getMemberId().equals(command.memberId())) {
			
			throw RentalAccessDeniedException.memberMismatch(rental.getId(), command.memberId());
		}
		
		if (rentalItem.getStatus() != RentalItemStatus.RENTING && rentalItem.getStatus() != RentalItemStatus.ACCEPTED) {
			
			throw new RentalStatusInvalidException(
					"진행 중이거나 승인된 상품만 연장할 수 있습니다. rentalItemId: " + command.rentalItemId() + ", status: "
							+ rentalItem.getStatus());
		}
		
		LocalDate oldEndDate = rentalItem.getEndDate();
		
		BigDecimal extraAmount = rentalItem.extendRental(command.newEndDate());
		
		accountFeignClient.charge(new ChargeWalletCommand(command.memberId(), rental.getId(), extraAmount));
		
		rental.updateTotalAmount(rental.getTotalAmount().add(extraAmount));
		rental.updateStatusFromItems();
		
		long extraDays = ChronoUnit.DAYS.between(oldEndDate, command.newEndDate());
		
		rentalEventLogService.logEvent(rental, RentalEventType.RENTAL_EXTENDED, Map.of("rentalId", rental.getId(), "rentalItemId", rentalItem.getId(), "oldEndDate", oldEndDate, "newEndDate", command.newEndDate(), "extraDays", extraDays, "extraAmount", extraAmount, "totalAmount", rental.getTotalAmount()));
	}
	
	@Transactional
	public void refundRentalItem(Long rentalItemId, Long memberId) {
		
		RentalItem rentalItem = rentalItemRepository.findById(rentalItemId)
				.orElseThrow(() -> new RentalItemNotFoundException(rentalItemId));
		Rental rental = rentalItem.getRental();
		
		if (rental == null) {
			
			throw new RentalNotFoundException(rentalItemId);
		}
		
		if (!rental.getMemberId().equals(memberId)) {
			
			throw RentalAccessDeniedException.memberMismatch(rental.getId(), memberId);
		}
		
		BigDecimal refundAmount = rentalItem.processRefund();
		accountFeignClient.refund(new RefundWalletCommand(memberId, rental.getId(), rentalItem.getId(), refundAmount));
		
		rental.updateStatusFromItems();
		
		rentalEventLogService.logEvent(rental, RentalEventType.RENTAL_REFUNDED, Map.of("rentalId", rental.getId(), "rentalItemId", rentalItem.getId(), "status", rental.getStatus()
				.name(), "itemStatus", rentalItem.getStatus().name(), "refundAmount", refundAmount));
	}
}
