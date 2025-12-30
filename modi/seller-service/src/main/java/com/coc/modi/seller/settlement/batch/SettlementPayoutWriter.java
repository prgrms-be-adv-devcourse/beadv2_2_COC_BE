package com.coc.modi.seller.settlement.batch;

import com.coc.modi.seller.exception.SellerSettlementNotFoundException;
import com.coc.modi.seller.settlement.domain.SellerSettlement;
import com.coc.modi.seller.settlement.domain.SellerSettlementStatus;
import com.coc.modi.seller.settlement.infrastructure.SellerSettlementJpaRepository;
import com.coc.modi.seller.settlement.infrastructure.client.wallet.WalletClientAdapter;
import com.coc.modi.seller.settlement.infrastructure.client.wallet.dto.SettlementPayoutRequest;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
public class SettlementPayoutWriter implements ItemWriter<SettlementPayoutItem> {

	private final WalletClientAdapter walletClientAdapter;
	private final SellerSettlementJpaRepository settlementRepository;

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
				markAsPaid(settlement);
				continue;
			}

			try {
				walletClientAdapter.payoutSettlement(new SettlementPayoutRequest(
						item.memberId(),
						item.settlementId(),
						amount
				));
				markAsPaid(settlement);
			} catch (FeignException.Conflict ex) {
				log.info("Settlement payout already processed. settlementId={}", item.settlementId());
				markAsPaid(settlement);
			}
		}
	}

	private void markAsPaid(SellerSettlement settlement) {

		if (settlement.getStatus() == SellerSettlementStatus.PAID) {
			return;
		}
		settlement.pay(LocalDateTime.now());
		settlementRepository.save(settlement);
	}
}
