package com.coc.modi.seller.settlement.batch;


import com.coc.modi.seller.settlement.application.SettlementNotificationService;
import com.coc.modi.seller.settlement.application.SettlementPayoutRequestPublisher;
import com.coc.modi.seller.settlement.exception.SellerSettlementNotFoundException;
import com.coc.modi.seller.settlement.domain.SellerSettlement;
import com.coc.modi.seller.settlement.domain.SellerSettlementStatus;
import com.coc.modi.seller.settlement.infrastructure.SellerSettlementJpaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
public class SettlementPayoutWriter implements ItemWriter<SettlementPayoutItem> {

	private final SellerSettlementJpaRepository settlementRepository;
	private final SettlementPayoutRequestPublisher settlementPayoutRequestPublisher;
	private final SettlementNotificationService settlementNotificationService;

	@Override
	public void write(Chunk<? extends SettlementPayoutItem> chunk) {

		if (chunk == null || chunk.isEmpty()) {
			return;
		}

		for (SettlementPayoutItem item : chunk) {
			if (item == null || item.settlementId() == null) {
				continue;
			}

			SellerSettlement settlement = settlementRepository.findById(item.settlementId())
					.orElseThrow(() -> new SellerSettlementNotFoundException("settlement not found. id=" + item.settlementId()));

			if (settlement.getStatus() != SellerSettlementStatus.READY) {
				continue;
			}

			BigDecimal amount = item.amount();
			if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
				log.info("Skip payout with non-positive amount. settlementId={}, amount={}", item.settlementId(), amount);
				markAsPaid(settlement);
				continue;
			}

			settlement.requestPayout();
			settlementRepository.save(settlement);
			settlementPayoutRequestPublisher.publish(
					item.settlementId(),
					item.sellerId(),
					item.memberId(),
					amount
			);
		}
	}

	private void markAsPaid(SellerSettlement settlement) {

		if (settlement.getStatus() == SellerSettlementStatus.PAID) {
			return;
		}
		settlement.pay(LocalDateTime.now());
		settlementRepository.save(settlement);
		settlementNotificationService.notifySettlementPaid(settlement);
	}
}
