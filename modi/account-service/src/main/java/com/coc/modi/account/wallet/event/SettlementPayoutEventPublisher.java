package com.coc.modi.account.wallet.event;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.coc.modi.account.wallet.event.outbox.SettlementPayoutOutbox;
import com.coc.modi.account.wallet.event.outbox.SettlementPayoutOutboxRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SettlementPayoutEventPublisher {

	private final SettlementPayoutOutboxRepository settlementPayoutOutboxRepository;

	public void publishCompleted(Long settlementId, Long sellerId, Long memberId, BigDecimal amount) {

		SettlementPayoutOutbox outbox = SettlementPayoutOutbox.completed(
				settlementId,
				sellerId,
				memberId,
				amount
		);
		settlementPayoutOutboxRepository.save(outbox);
	}

	public void publishFailed(Long settlementId,
							  Long sellerId,
							  Long memberId,
							  BigDecimal amount,
							  String failureReason) {

		SettlementPayoutOutbox outbox = SettlementPayoutOutbox.failed(
				settlementId,
				sellerId,
				memberId,
				amount,
				failureReason
		);
		settlementPayoutOutboxRepository.save(outbox);
	}
}
