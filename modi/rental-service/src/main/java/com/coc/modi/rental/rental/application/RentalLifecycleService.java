package com.coc.modi.rental.rental.application;

import com.coc.modi.rental.rental.application.dto.RentalReturnCommand;
import com.coc.modi.rental.rental.application.dto.RentalReturnResponse;
import com.coc.modi.rental.rental.application.dto.ExtendRentalCommand;
import com.coc.modi.rental.rental.domain.Rental;
import com.coc.modi.rental.rental.domain.RentalEventType;
import com.coc.modi.rental.rental.domain.RentalItem;
import com.coc.modi.rental.rental.infrastructure.client.AccountClientAdapter;
import com.coc.modi.rental.rental.infrastructure.client.dto.ChargeWalletCommand;
import com.coc.modi.rental.rental.infrastructure.client.dto.RefundWalletCommand;
import com.coc.modi.rental.rental.domain.RentalQueryRepository;
import com.coc.modi.rental.rental.exception.RentalStatusInvalidException;
import com.coc.modi.kafka.event.RentalClosedEvent;
import com.coc.modi.kafka.event.RentalReturnedEvent;
import com.coc.modi.rental.outbox.RentalOutboxService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RentalLifecycleService {

	private static final int SEQUENCE_RETURNED = 1;
	private static final String CLOSED_TYPE_RETURNED = "RETURNED";
	
	private final RentalAppSupport rentalAppSupport;
	private final AccountClientAdapter accountClientAdapter;
	private final RentalEventLogService rentalEventLogService;
	private final RentalQueryRepository rentalQueryRepository;
	private final RentalOutboxService rentalOutboxService;
	
	@Transactional
	public void stratRenting(Long rentalItemId, Long memberId) {
		
		RentalItem rentalItem = rentalAppSupport.loadRentalItem(rentalItemId);
		rentalAppSupport.requireSeller(rentalItem.getSellerId(), memberId);
		
		rentalItem.startRenting(LocalDate.now());
		
		Rental rental = rentalAppSupport.requireRental(rentalItem);
		rental.recalculateAmountsAndStatus();
		
		rentalEventLogService.logEvent(rental, RentalEventType.ITEM_HANDED_OVER,
				Map.of("rentalId", rental.getId(), "rentalItemId", rentalItem.getId(),
						"itemStatus", rentalItem.getStatus().name(), "rentalStatus", rental.getStatus().name()));
	}
	
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
		BigDecimal depositAmount = rentalItem.getSecurityDepositAmount() == null
				? BigDecimal.ZERO
				: rentalItem.getSecurityDepositAmount();
		BigDecimal depositRefundAmount = depositAmount.subtract(totalFee);
		if (depositRefundAmount.signum() < 0) {
			depositRefundAmount = BigDecimal.ZERO;
		}
		
		Map<String, Object> payload = new java.util.LinkedHashMap<>();
		payload.put("rentalId", rental.getId());
		payload.put("rentalItemId", rentalItem.getId());
		payload.put("rentalStatus", rental.getStatus().name());
		payload.put("itemStatus", rentalItem.getStatus().name());
		payload.put("damageFee", damageFee);
		payload.put("lateFee", lateFee);
		payload.put("extraFeeTotal", totalFee);
		payload.put("depositAmount", depositAmount);
		payload.put("depositRefundAmount", depositRefundAmount);
		payload.put("damageReason", command.damageReason());
		payload.put("lateReason", command.lateReason());
		payload.put("memo", command.memo());

		rentalEventLogService.logEvent(rental, RentalEventType.RENTAL_RETURNED, payload);

		BigDecimal rentalAmount = rentalItem.calculateRentalAmount();
		RentalReturnedEvent event = RentalReturnedEvent.of(
				rentalItem.getId(),
				rental.getMemberId(),
				rentalItem.getSellerId(),
				rentalItem.getProductId(),
				rentalAmount,
				rentalItem.getStatus().name(),
				rentalItem.getReturnedAt()
		);
		rentalOutboxService.enqueueRentalReturnedEvent(rentalItem.getId(), event);

		if (depositRefundAmount.signum() > 0) {
			String requestId = WalletRequestId.depositRefund(rentalItem.getId());
			accountClientAdapter.refund(new RefundWalletCommand(
					rental.getMemberId(),
					rental.getId(),
					rentalItem.getId(),
					depositRefundAmount,
					requestId
			));
			registerDepositRefundCompensationOnRollback(rental, rentalItem, depositRefundAmount);
		}

		RentalClosedEvent closedEvent = RentalClosedEvent.of(
				rentalItem.getId(),
				rental.getMemberId(),
				rentalItem.getSellerId(),
				rentalItem.getProductId(),
				rentalAmount,
				CLOSED_TYPE_RETURNED,
				rentalItem.getReturnedAt(),
				null,
				SEQUENCE_RETURNED
		);
		rentalOutboxService.enqueueRentalClosedEvent(rentalItem.getId(), closedEvent);
		
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
		
		if (!rentalItem.getStatus().isRenting() && !rentalItem.getStatus().isPaid()) {
			
			throw new RentalStatusInvalidException("연장은 Paid / Renting 상태에서만 가능합니다. rentalItemId= " +
					rentalItem.getId() + " status= " + rentalItem.getStatus());
		}
		
		LocalDate oldEndDate = rentalItem.getEndDate();
		
		validateAvailability(rentalItem.getProductId(), oldEndDate.plusDays(1), command.newEndDate(), rentalItem.getId());
		
		BigDecimal extraAmount = rentalItem.extendRental(command.newEndDate());

		String chargeRequestId = WalletRequestId.extend(rentalItem.getId(), command.newEndDate());
		accountClientAdapter.charge(new ChargeWalletCommand(command.memberId(), rental.getId(), extraAmount, chargeRequestId));

		registerExtendCompensationOnRollback(rentalItem, rental, extraAmount, command);
		
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
	
	private void validateAvailability(Long productId, LocalDate startDate, LocalDate endDate, Long excludeRentalItemId) {
		boolean hasOverlap = rentalQueryRepository.existsOverlappingRentalItem(productId, startDate, endDate, excludeRentalItemId);
		if (hasOverlap) {
			throw new RentalStatusInvalidException(
					"요청한 기간에 이미 예약된 상품입니다. productId=" + productId + ", startDate=" + startDate + ", endDate=" + endDate);
		}
	}

	private void registerExtendCompensationOnRollback(RentalItem rentalItem, Rental rental, BigDecimal extraAmount, ExtendRentalCommand command) {

		if (!TransactionSynchronizationManager.isActualTransactionActive()) {
			return;
		}

		Long rentalItemId = rentalItem.getId();
		if (rentalItemId == null) {
			return;
		}

		Long rentalId = rental.getId();
		LocalDate newEndDate = command.newEndDate();

		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCompletion(int status) {
				if (status != STATUS_ROLLED_BACK) {
					return;
				}
				String requestId = WalletRequestId.extendCompRefund(rentalItemId, newEndDate);
				try {
					accountClientAdapter.refund(new RefundWalletCommand(
							command.memberId(),
							rentalId,
							rentalItemId,
							extraAmount,
							requestId
					));
				} catch (Exception ex) {
					log.error("연장 결제 보상 환불 실패. rentalId={}, rentalItemId={}", rentalId, rentalItemId, ex);
				}
			}
		});
	}

	private void registerDepositRefundCompensationOnRollback(Rental rental,
															RentalItem rentalItem,
															BigDecimal refundAmount) {

		if (!TransactionSynchronizationManager.isActualTransactionActive()) {
			return;
		}

		Long rentalItemId = rentalItem.getId();
		if (rentalItemId == null) {
			return;
		}

		Long rentalId = rental.getId();

		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCompletion(int status) {
				if (status != STATUS_ROLLED_BACK) {
					return;
				}
				String requestId = WalletRequestId.depositRefundCompCharge(rentalItemId);
				try {
					accountClientAdapter.charge(new ChargeWalletCommand(
							rental.getMemberId(),
							rentalId,
							refundAmount,
							requestId
					));
				} catch (Exception ex) {
					log.error("보증금 환불 보상 차지 실패. rentalId={}, rentalItemId={}", rentalId, rentalItemId, ex);
				}
			}
		});
	}
}
