package com.coc.modi.rental.rental.application;

import com.coc.modi.rental.rental.application.dto.RentalReturnCommand;
import com.coc.modi.rental.rental.application.dto.RentalReturnResponse;
import com.coc.modi.rental.rental.application.dto.ExtendRentalCommand;
import com.coc.modi.rental.rental.domain.Rental;
import com.coc.modi.rental.rental.domain.RentalEventType;
import com.coc.modi.rental.rental.domain.RentalItem;
import com.coc.modi.rental.rental.infrastructure.client.AccountFeignClient;
import com.coc.modi.rental.rental.infrastructure.client.dto.ChargeWalletCommand;
import com.coc.modi.rental.rental.infrastructure.client.dto.RefundWalletCommand;
import com.coc.modi.rental.rental.domain.RentalQueryRepository;
import com.coc.modi.rental.rental.exception.RentalStatusInvalidException;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import feign.FeignException;

@Service
@RequiredArgsConstructor
public class RentalLifecycleService {
	
	private final RentalAppSupport rentalAppSupport;
	private final AccountFeignClient accountFeignClient;
	private final RentalEventLogService rentalEventLogService;
	private final RentalQueryRepository rentalQueryRepository;
	
	@Transactional
	public void cancelRentalItem(Long rentalItemId, Long memberId) {
		
		RentalItem rentalItem = rentalAppSupport.loadRentalItem(rentalItemId);
		Rental rental = rentalAppSupport.requireRental(rentalItem);
		
		rentalAppSupport.requireMember(rental, memberId);
		
		rentalItem.cancelByMemberRequest();
		
		rental.recalculateAmountsAndStatus();
		
		rentalEventLogService.logEvent(rental, RentalEventType.RENTAL_CANCELED, Map.of("rentalId", rental.getId(), "rentalItemid", rentalItemId, "itemStatus", rentalItem.getStatus()
				.name(), "rentalStatus", rental.getStatus().name()));
	}
	
	@Transactional
	public RentalReturnResponse completeReturn(RentalReturnCommand command) {
		
		RentalItem rentalItem = rentalAppSupport.loadRentalItem(command.rentalItemId());
		
		rentalAppSupport.requireSeller(rentalItem.getSellerId(), command.memberId());
		
		Rental rental = rentalAppSupport.requireRental(rentalItem);
		
		rentalItem.processReturn();
		rental.recalculateAmountsAndStatus();
		
		BigDecimal damageFee = command.damageFee() == null ? BigDecimal.ZERO : command.damageFee();
		BigDecimal lateFee = command.lateFee() == null ? BigDecimal.ZERO : command.lateFee();
		BigDecimal totalFee = damageFee.add(lateFee);
		
		rentalEventLogService.logEvent(rental, RentalEventType.RENTAL_RETURNED,
				Map.of("rentalId", rental.getId(),
						"rentalItemId", rentalItem.getId(),
						"rentalStatus", rental.getStatus().name(),
						"itemStatus", rentalItem.getStatus().name(),
						"damageFee", damageFee,
						"lateFee", lateFee,
						"extraFeeTotal", totalFee,
						"damageReason", command.damageReason(),
						"lateReason", command.lateReason(),
						"memo", command.memo()));
		
		return new RentalReturnResponse(
				rental.getId(),
				rentalItem.getId(),
				rentalItem.getStatus().name(),
				totalFee.toString());
	}
	
	@Transactional
	public void extendRentalItem(ExtendRentalCommand command) {
		
		RentalItem rentalItem = rentalAppSupport.loadRentalItem(command.rentalItemId());
		Rental rental = rentalAppSupport.requireRental(rentalItem);
		
		rentalAppSupport.requireMember(rental, command.memberId());
		
		if (!rentalItem.getStatus().isRenting()) {
			
			throw new RentalStatusInvalidException("연장은 Renting 상태에서만 가능합니다. rentalItemId= " +
					rentalItem.getId() + " status= " + rentalItem.getStatus());
		}
		
		LocalDate oldEndDate = rentalItem.getEndDate();
		
		validateAvailability(rentalItem.getProductId(), oldEndDate.plusDays(1), command.newEndDate(), rentalItem.getId());
		
		BigDecimal extraAmount = rentalItem.extendRental(command.newEndDate());
		
		try {
			accountFeignClient.charge(new ChargeWalletCommand(command.memberId(), rental.getId(), extraAmount));
		} catch (FeignException ex) {
			throw new RentalStatusInvalidException("지갑 추가 결제에 실패했습니다.");
		}
		
		rental.recalculateAmountsAndStatus();
		
		long extraDays = ChronoUnit.DAYS.between(oldEndDate, command.newEndDate());
		
		rentalEventLogService.logEvent(rental, RentalEventType.RENTAL_EXTENDED,
				Map.of("rentalId", rental.getId(),
						"rentalItemId", rentalItem.getId(),
						"oldEndDate", oldEndDate,
						"newEndDate", command.newEndDate(),
						"extraDays", extraDays,
						"extraAmount", extraAmount,
						"totalAmount", rental.getTotalAmount()));
	}
	
	@Transactional
	public void refundRentalItem(Long rentalItemId, Long memberId) {
		RentalItem rentalItem = rentalAppSupport.loadRentalItem(rentalItemId);
		Rental rental = rentalAppSupport.requireRental(rentalItem);
		
		rentalAppSupport.requireMember(rental, memberId);
		
		BigDecimal refundAmount = rentalItem.processRefund();
		
		try {
			accountFeignClient.refund(new RefundWalletCommand(memberId, rental.getId(), rentalItem.getId(), refundAmount));
		} catch (FeignException ex) {
			throw new RentalStatusInvalidException("환불 처리 중 지갑 서비스 호출에 실패했습니다.");
		}
		
		rental.recalculateAmountsAndStatus();
		
		rentalEventLogService.logEvent(rental, RentalEventType.RENTAL_REFUNDED,
				Map.of("rentalId", rental.getId(),
						"rentalItemId", rentalItem.getId(),
						"rentalStatus", rental.getStatus().name(),
						"itemStatus", rentalItem.getStatus().name(),
						"refundAmount", refundAmount));
	}
	
	private void validateAvailability(Long productId, LocalDate startDate, LocalDate endDate, Long excludeRentalItemId) {
		boolean hasOverlap = rentalQueryRepository.existsOverlappingRentalItem(productId, startDate, endDate, excludeRentalItemId);
		if (hasOverlap) {
			throw new RentalStatusInvalidException(
					"요청한 기간에 이미 예약된 상품입니다. productId=" + productId + ", startDate=" + startDate + ", endDate=" + endDate);
		}
	}
}
