package com.coc.modi.account.wallet.event;

import java.math.BigDecimal;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.coc.modi.kafka.event.SettlementPayoutCompletedEvent;
import com.coc.modi.kafka.event.SettlementPayoutFailedEvent;
import com.coc.modi.kafka.topic.KafkaTopics;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SettlementPayoutEventPublisher {

	private final KafkaTemplate<String, Object> kafkaTemplate;

	public void publishCompleted(Long settlementId, Long sellerId, Long memberId, BigDecimal amount) {

		SettlementPayoutCompletedEvent event = SettlementPayoutCompletedEvent.of(
				settlementId,
				sellerId,
				memberId,
				amount
		);

		kafkaTemplate.send(
				KafkaTopics.SETTLEMENT_PAYOUT_COMPLETED_EVENTS,
				settlementId.toString(),
				event
		);
	}

	public void publishFailed(Long settlementId,
							  Long sellerId,
							  Long memberId,
							  BigDecimal amount,
							  String failureReason) {

		SettlementPayoutFailedEvent event = SettlementPayoutFailedEvent.of(
				settlementId,
				sellerId,
				memberId,
				amount,
				failureReason
		);

		kafkaTemplate.send(
				KafkaTopics.SETTLEMENT_PAYOUT_FAILED_EVENTS,
				settlementId.toString(),
				event
		);
	}
}
