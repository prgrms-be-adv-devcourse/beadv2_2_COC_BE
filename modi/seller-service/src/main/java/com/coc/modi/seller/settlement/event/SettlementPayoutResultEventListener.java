package com.coc.modi.seller.settlement.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.coc.modi.kafka.event.SettlementPayoutCompletedEvent;
import com.coc.modi.kafka.event.SettlementPayoutFailedEvent;
import com.coc.modi.kafka.topic.KafkaTopics;
import com.coc.modi.seller.settlement.application.SettlementPayoutResultService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SettlementPayoutResultEventListener {

	private final SettlementPayoutResultService settlementPayoutResultService;

	@KafkaListener(
			topics = KafkaTopics.ACCOUNT_SETTLEMENT_PAYOUT_COMPLETED,
			groupId = "seller-service",
			containerFactory = "settlementPayoutCompletedKafkaListenerContainerFactory"
	)
	public void onPayoutCompleted(SettlementPayoutCompletedEvent event) {

		settlementPayoutResultService.handleCompleted(event);
	}

	@KafkaListener(
			topics = KafkaTopics.ACCOUNT_SETTLEMENT_PAYOUT_FAILED,
			groupId = "seller-service",
			containerFactory = "settlementPayoutFailedKafkaListenerContainerFactory"
	)
	public void onPayoutFailed(SettlementPayoutFailedEvent event) {

		settlementPayoutResultService.handleFailed(event);
	}
}
