package com.coc.modi.seller.settlement.application;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coc.modi.kafka.event.SettlementPayoutCompletedEvent;
import com.coc.modi.kafka.event.SettlementPayoutFailedEvent;
import com.coc.modi.seller.settlement.domain.SellerSettlement;
import com.coc.modi.seller.settlement.domain.SellerSettlementRepository;
import com.coc.modi.seller.settlement.domain.SellerSettlementStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementPayoutResultService {

	private final SellerSettlementRepository sellerSettlementRepository;
	private final SettlementNotificationService settlementNotificationService;

	@Transactional
	public void handleCompleted(SettlementPayoutCompletedEvent event) {

		if (event == null || !isValid(event.settlementId(), event.sellerId(), event.memberId(), event.amount())) {
			return;
		}

		SellerSettlement settlement = sellerSettlementRepository.findById(event.settlementId())
				.orElse(null);
		if (settlement == null) {
			log.warn("정산 지급 완료 처리 실패: 정산 정보를 찾을 수 없습니다. settlementId={}", event.settlementId());
			return;
		}
		if (settlement.getStatus() == SellerSettlementStatus.PAID
				|| settlement.getStatus() == SellerSettlementStatus.CANCELED) {
			return;
		}

		settlement.pay(LocalDateTime.now());
		sellerSettlementRepository.save(settlement);
		settlementNotificationService.notifySettlementPaid(settlement);
	}

	@Transactional
	public void handleFailed(SettlementPayoutFailedEvent event) {

		if (event == null || !isValid(event.settlementId(), event.sellerId(), event.memberId(), event.amount())) {
			return;
		}

		SellerSettlement settlement = sellerSettlementRepository.findById(event.settlementId())
				.orElse(null);
		if (settlement == null) {
			log.warn("정산 지급 실패 처리 실패: 정산 정보를 찾을 수 없습니다. settlementId={}", event.settlementId());
			return;
		}
		if (settlement.getStatus() == SellerSettlementStatus.PAID
				|| settlement.getStatus() == SellerSettlementStatus.CANCELED) {
			return;
		}

		settlement.fail(event.failureReason());
		sellerSettlementRepository.save(settlement);
	}

	private boolean isValid(Long settlementId, Long sellerId, Long memberId, BigDecimal amount) {

		return settlementId != null
				&& sellerId != null
				&& memberId != null
				&& amount != null
				&& amount.compareTo(BigDecimal.ZERO) > 0;
	}
}
