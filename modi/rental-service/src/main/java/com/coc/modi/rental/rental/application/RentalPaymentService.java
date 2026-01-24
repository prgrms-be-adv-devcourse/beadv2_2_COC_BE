package com.coc.modi.rental.rental.application;

import com.coc.modi.rental.rental.application.dto.PayRentalResponse;
import com.coc.modi.rental.rental.domain.Rental;
import com.coc.modi.rental.rental.domain.RentalEventType;
import com.coc.modi.rental.rental.domain.RentalItem;
import com.coc.modi.rental.rental.domain.RentalItemStatus;
import com.coc.modi.rental.rental.domain.RentalRepository;
import com.coc.modi.rental.rental.domain.RentalStatus;
import com.coc.modi.rental.rental.exception.RentalAccessDeniedException;
import com.coc.modi.rental.rental.exception.RentalNotFoundException;
import com.coc.modi.rental.rental.exception.RentalStatusInvalidException;
import com.coc.modi.rental.rental.infrastructure.client.AccountClientAdapter;
import com.coc.modi.rental.rental.infrastructure.client.dto.ChargeWalletCommand;
import com.coc.modi.kafka.event.RentalClosedEvent;
import com.coc.modi.rental.rental.infrastructure.client.dto.RefundWalletCommand;
import com.coc.modi.rental.rental.infrastructure.client.dto.WalletInfoResponse;
import com.coc.modi.rental.outbox.RentalOutboxService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RentalPaymentService {

	private static final int SEQUENCE_REFUNDED = 2;
	private static final String CLOSED_TYPE_REFUNDED = "REFUNDED";
	
	private final RentalRepository rentalRepository;
	private final AccountClientAdapter accountClientAdapter;
	private final RentalEventLogService rentalEventLogService;
	private final RentalAppSupport rentalAppSupport;
	private final RentalOutboxService rentalOutboxService;
	
	@Transactional
	public PayRentalResponse completePayment(Long rentalId, Long memberId) {
		
		Rental rental = rentalRepository.findById(rentalId)
				.orElseThrow(() -> new RentalNotFoundException(rentalId));
		
		if (!rental.getMemberId().equals(memberId)) {
			throw RentalAccessDeniedException.memberMismatch(rental.getId(), memberId);
		}
		
		if (rental.getItems() == null || rental.getItems().isEmpty()) {
			throw new RentalStatusInvalidException("결제할 대여 상품이 없습니다. rentalId=" + rentalId);
		}
		
		// 승인 상태/총액 정합성
		rental.recalculateAmountsAndStatus();
		
		RentalStatus rentalStatus = rental.getStatus();
		if (rentalStatus == RentalStatus.CANCELED || rentalStatus == RentalStatus.COMPLETED) {
			throw new RentalStatusInvalidException(
					"취소/완료된 대여는 결제 불가. rentalId=" + rentalId + ", status=" + rentalStatus);
		}
		
		if (rentalStatus != RentalStatus.ACCEPTED) {
			throw new RentalStatusInvalidException(
					"모든 아이템이 승인(ACCEPTED)일 때만 결제 가능. rentalId=" + rentalId + ", status=" + rentalStatus);
		}
		
		BigDecimal totalAmount = rental.getTotalAmount();
		BigDecimal totalDepositAmount = rental.calculateTotalDepositAmount();
		BigDecimal totalChargeAmount = totalAmount.add(totalDepositAmount);

		String chargeRequestId = WalletRequestId.payment(rental.getId());
		
		WalletInfoResponse walletInfoResponse = accountClientAdapter.charge(
				new ChargeWalletCommand(memberId, rental.getId(), totalChargeAmount, chargeRequestId));

		registerPaymentCompensationOnRollback(rental, memberId);
		
		LocalDateTime paidAt = LocalDateTime.now();
		
		rental.getItems().stream()
				.filter(item -> item.getStatus() == RentalItemStatus.ACCEPTED)
				.forEach(RentalItem::markPaid);
		rental.markPaid(paidAt);
		
		rentalEventLogService.logEvent(rental, RentalEventType.PAID,
				Map.of("rentalId", rental.getId(),
						"paidAt", paidAt,
						"amount", totalChargeAmount,
						"rentalAmount", totalAmount,
						"depositAmount", totalDepositAmount,
						"walletBalance", walletInfoResponse.balance(),
						"rentalStatus", rental.getStatus().name()));
		
		return PayRentalResponse.create(rental, totalChargeAmount, walletInfoResponse.balance(), paidAt);
	}
	
	
	@Transactional
	public void refundRentalItem(Long rentalItemId, Long memberId) {
		RentalItem rentalItem = rentalAppSupport.loadRentalItem(rentalItemId);
		Rental rental = rentalAppSupport.requireRental(rentalItem);
		
		rentalAppSupport.requireMember(rental, memberId);

		if (isAlreadyRefunded(rental, rentalItem)) {
			return;
		}
		
		BigDecimal refundAmount = rentalItem.processRefund();
		
		String refundRequestId = WalletRequestId.refund(rentalItem.getId());
		accountClientAdapter.refund(new RefundWalletCommand(memberId, rental.getId(), rentalItem.getId(), refundAmount, refundRequestId));

		registerRefundCompensationOnRollback(rental, rentalItem, refundAmount);
		
		rental.recalculateAmountsAndStatus();
		
		rentalEventLogService.logEvent(rental, RentalEventType.RENTAL_REFUNDED,
				Map.of("rentalId", rental.getId(),
						"rentalItemId", rentalItem.getId(),
						"rentalStatus", rental.getStatus().name(),
						"itemStatus", rentalItem.getStatus().name(),
						"refundAmount", refundAmount));

		if (rentalItem.getReturnedAt() != null) {
			RentalClosedEvent closedEvent = RentalClosedEvent.of(
					rentalItem.getId(),
					rental.getMemberId(),
					rentalItem.getSellerId(),
					rentalItem.getProductId(),
					rentalItem.calculateRentalAmount(),
					CLOSED_TYPE_REFUNDED,
					rentalItem.getReturnedAt(),
					rentalItem.getCanceledAt(),
					SEQUENCE_REFUNDED
			);
			rentalOutboxService.enqueueRentalClosedEvent(rentalItem.getId(), closedEvent);
		}
	}

	private boolean isAlreadyRefunded(Rental rental, RentalItem rentalItem) {
		if (rentalItem.getCanceledAt() == null) {
			return false;
		}
		if (rentalItem.getStatus() == RentalItemStatus.RETURNED) {
			return true;
		}
		return rentalItem.getStatus() == RentalItemStatus.CANCELED && rental.getPaidAt() != null;
	}

	private void registerPaymentCompensationOnRollback(Rental rental, Long memberId) {

		if (!TransactionSynchronizationManager.isActualTransactionActive()) {
			return;
		}

		List<RefundTarget> refundTargets = rental.getItems().stream()
				.filter(item -> item.getStatus() == RentalItemStatus.ACCEPTED)
				.map(item -> new RefundTarget(item.getId(), item.calculateChargeAmount()))
				.toList();

		if (refundTargets.isEmpty()) {
			return;
		}

		Long rentalId = rental.getId();

		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCompletion(int status) {
				if (status != STATUS_ROLLED_BACK) {
					return;
				}
				for (RefundTarget target : refundTargets) {
					Long rentalItemId = target.rentalItemId();
					if (rentalItemId == null) {
						continue;
					}
					BigDecimal amount = target.amount();
					String requestId = WalletRequestId.paymentCompRefund(rentalId, rentalItemId);
					try {
						accountClientAdapter.refund(new RefundWalletCommand(
								memberId,
								rentalId,
								rentalItemId,
								amount,
								requestId
						));
					} catch (Exception ex) {
						log.error("렌탈 결제 보상 환불 실패. rentalId={}, rentalItemId={}", rentalId, rentalItemId, ex);
					}
				}
			}
		});
	}

	private void registerRefundCompensationOnRollback(Rental rental, RentalItem rentalItem, BigDecimal refundAmount) {

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
				String requestId = WalletRequestId.refundCompCharge(rentalItemId);
				try {
					accountClientAdapter.charge(new ChargeWalletCommand(
							rental.getMemberId(),
							rentalId,
							refundAmount,
							requestId
					));
				} catch (Exception ex) {
					log.error("렌탈 환불 보상 차지 실패. rentalId={}, rentalItemId={}", rentalId, rentalItemId, ex);
				}
			}
		});
	}

	private record RefundTarget(Long rentalItemId, BigDecimal amount) {
	}
}
