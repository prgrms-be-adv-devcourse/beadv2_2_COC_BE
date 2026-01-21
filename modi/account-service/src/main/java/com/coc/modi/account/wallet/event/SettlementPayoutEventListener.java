package com.coc.modi.account.wallet.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.coc.modi.account.wallet.application.WalletCommandService;
import com.coc.modi.account.wallet.exception.AccountException;
import com.coc.modi.kafka.event.SettlementPayoutRequestedEvent;
import com.coc.modi.kafka.topic.KafkaTopics;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementPayoutEventListener {

	private final WalletCommandService walletCommandService;
	private final SettlementPayoutEventPublisher settlementPayoutEventPublisher;

	@KafkaListener(
			topics = KafkaTopics.SETTLEMENT_PAYOUT_EVENTS,
			groupId = "account-service",
			containerFactory = "settlementPayoutKafkaListenerContainerFactory"
	)
	public void onSettlementPayoutRequested(SettlementPayoutRequestedEvent event) {

		if (event == null) {
			return;
		}
		if (event.settlementId() == null || event.sellerId() == null || event.memberId() == null || event.amount() == null) {
			log.warn("Settlement payout event missing required fields. event={}", event);
			return;
		}

		try {
			boolean processed = walletCommandService.payoutSettlement(
					event.memberId(),
					event.settlementId(),
					event.amount()
			);
			if (!processed) {
				log.info("Settlement payout already processed. settlementId={}", event.settlementId());
			}
			settlementPayoutEventPublisher.publishCompleted(
					event.settlementId(),
					event.sellerId(),
					event.memberId(),
					event.amount()
			);
		} catch (AccountException ex) {
			log.warn("Settlement payout failed. settlementId={} reason={}",
					event.settlementId(),
					ex.getMessage());
			settlementPayoutEventPublisher.publishFailed(
					event.settlementId(),
					event.sellerId(),
					event.memberId(),
					event.amount(),
					ex.getMessage()
			);
		} catch (Exception ex) {
			log.warn("Settlement payout unexpected error. settlementId={}", event.settlementId(), ex);
			settlementPayoutEventPublisher.publishFailed(
					event.settlementId(),
					event.sellerId(),
					event.memberId(),
					event.amount(),
					"정산 지급 처리 실패"
			);
		}
	}
}
