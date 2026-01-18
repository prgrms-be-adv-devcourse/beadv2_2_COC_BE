package com.coc.modi.account.wallet.event.outbox;

import java.time.Instant;

import com.coc.modi.kafka.event.SettlementPayoutCompletedEvent;
import com.coc.modi.kafka.event.SettlementPayoutFailedEvent;
import com.coc.modi.kafka.topic.KafkaTopics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementPayoutOutboxPublishWorker {
	
	private final SettlementPayoutOutboxRepository settlementPayoutOutboxRepository;
	private final KafkaTemplate<String, Object> kafkaTemplate;
	
	@Value("${settlement.payout.outbox.retry.max-attempts:10}")
	private int maxAttempts;
	
	@Value("${settlement.payout.outbox.retry.initial-delay-seconds:5}")
	private long initialDelaySeconds;
	
	@Value("${settlement.payout.outbox.retry.max-delay-seconds:300}")
	private long maxDelaySeconds;
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void publishOne(Long outboxId) {
		
		if (outboxId == null) {
			return;
		}
		
		Instant now = Instant.now();
		int claimed = settlementPayoutOutboxRepository.claim(
				outboxId,
				SettlementPayoutOutboxStatus.PENDING,
				SettlementPayoutOutboxStatus.PROCESSING,
				now
		);
		if (claimed != 1) {
			return;
		}
		
		SettlementPayoutOutbox outbox = settlementPayoutOutboxRepository.findById(outboxId).orElse(null);
		if (outbox == null) {
			return;
		}
		
		try {
			publish(outbox);
			outbox.markSent();
		} catch (Exception ex) {
			if (ex instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
			handleFailure(outbox, ex, now);
		}
	}
	
	private void publish(SettlementPayoutOutbox outbox) throws Exception {
		
		if (outbox.getEventType() == SettlementPayoutOutboxType.COMPLETED) {
			SettlementPayoutCompletedEvent event = new SettlementPayoutCompletedEvent(
					outbox.getEventId(),
					outbox.getOccurredAt(),
					outbox.getSettlementId(),
					outbox.getSellerId(),
					outbox.getMemberId(),
					outbox.getAmount()
			);
			kafkaTemplate.send(
					KafkaTopics.SETTLEMENT_PAYOUT_COMPLETED_EVENTS,
					outbox.getSettlementId().toString(),
					event
			).get();
			return;
		}
		
		SettlementPayoutFailedEvent event = new SettlementPayoutFailedEvent(
				outbox.getEventId(),
				outbox.getOccurredAt(),
				outbox.getSettlementId(),
				outbox.getSellerId(),
				outbox.getMemberId(),
				outbox.getAmount(),
				outbox.getFailureReason()
		);
		kafkaTemplate.send(
				KafkaTopics.SETTLEMENT_PAYOUT_FAILED_EVENTS,
				outbox.getSettlementId().toString(),
				event
		).get();
	}
	
	private void handleFailure(SettlementPayoutOutbox outbox, Exception ex, Instant now) {
		
		String errorMessage = summarizeError(ex);
		int nextRetryCount = outbox.getRetryCount() + 1;
		if (nextRetryCount >= maxAttempts) {
			outbox.markFailed(errorMessage);
			log.warn("정산 지급 outbox 발행 실패(최대 재시도 초과). outboxId={} settlementId={}",
					outbox.getId(), outbox.getSettlementId(), ex);
			return;
		}
		
		Instant nextAttemptAt = now.plusSeconds(calculateDelaySeconds(nextRetryCount));
		outbox.markRetry(nextAttemptAt, errorMessage);
		log.warn("정산 지급 outbox 재시도 예약. outboxId={} settlementId={} retryCount={} nextAttemptAt={}",
				outbox.getId(), outbox.getSettlementId(), nextRetryCount, nextAttemptAt, ex);
	}
	
	private long calculateDelaySeconds(int retryCount) {
		
		long rawDelay = Math.round(initialDelaySeconds * Math.pow(2, retryCount - 1));
		return Math.min(rawDelay, maxDelaySeconds);
	}
	
	private String summarizeError(Exception ex) {
		
		String message = ex.getMessage();
		if (message == null || message.isBlank()) {
			message = ex.getClass().getSimpleName();
		}
		if (message.length() > 1000) {
			return message.substring(0, 1000);
		}
		return message;
	}
}
