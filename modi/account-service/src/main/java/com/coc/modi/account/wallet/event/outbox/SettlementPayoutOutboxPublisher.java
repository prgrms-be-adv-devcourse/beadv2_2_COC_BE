package com.coc.modi.account.wallet.event.outbox;

import java.time.Instant;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SettlementPayoutOutboxPublisher {
	
	private final SettlementPayoutOutboxRepository settlementPayoutOutboxRepository;
	private final SettlementPayoutOutboxPublishWorker settlementPayoutOutboxPublishWorker;
	
	@Value("${settlement.payout.outbox.publish-batch-size:50}")
	private int publishBatchSize;
	
	@Scheduled(fixedDelayString = "${settlement.payout.outbox.publish-interval-ms:5000}")
	public void publishPending() {
		
		Instant now = Instant.now();
		List<SettlementPayoutOutbox> outboxes = settlementPayoutOutboxRepository.findReadyToPublish(
				SettlementPayoutOutboxStatus.PENDING,
				now,
				PageRequest.of(0, publishBatchSize)
		);
		for (SettlementPayoutOutbox outbox : outboxes) {
			settlementPayoutOutboxPublishWorker.publishOne(outbox.getId());
		}
	}
}
