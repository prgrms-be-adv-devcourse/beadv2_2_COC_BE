package com.coc.modi.seller.notification;

import java.time.Instant;

import com.coc.modi.kafka.event.NotificationEvent;
import com.coc.modi.kafka.topic.KafkaTopics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationOutboxPublishWorker {

	private final NotificationOutboxRepository notificationOutboxRepository;
	private final KafkaTemplate<String, Object> kafkaTemplate;

	@Value("${notification.outbox.retry.max-attempts:10}")
	private int maxAttempts;

	@Value("${notification.outbox.retry.initial-delay-seconds:5}")
	private long initialDelaySeconds;

	@Value("${notification.outbox.retry.max-delay-seconds:300}")
	private long maxDelaySeconds;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void publishOne(Long outboxId) {

		if (outboxId == null) {
			return;
		}

		Instant now = Instant.now();
		int claimed = notificationOutboxRepository.claim(
				outboxId,
				NotificationOutboxStatus.PENDING,
				NotificationOutboxStatus.PROCESSING,
				now
		);
		if (claimed != 1) {
			return;
		}

		NotificationOutbox outbox = notificationOutboxRepository.findById(outboxId).orElse(null);
		if (outbox == null) {
			return;
		}

		NotificationEvent event = new NotificationEvent(
				outbox.getEventId(),
				outbox.getOccurredAt(),
				outbox.getReceiverId(),
				outbox.getType(),
				outbox.getTitle(),
				outbox.getContent(),
				outbox.getReferenceType(),
				outbox.getReferenceId()
		);

		try {
			kafkaTemplate.send(
					KafkaTopics.NOTIFICATION_EVENTS,
					outbox.getReceiverId().toString(),
					event
			).get();
			outbox.markSent();
		} catch (Exception ex) {
			if (ex instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
			handleFailure(outbox, ex, now);
		}
	}

	private void handleFailure(NotificationOutbox outbox, Exception ex, Instant now) {

		String errorMessage = summarizeError(ex);
		int nextRetryCount = outbox.getRetryCount() + 1;
		if (nextRetryCount >= maxAttempts) {
			outbox.markFailed(errorMessage);
			log.warn("알림 outbox 발행 실패(최대 재시도 초과). outboxId={} eventId={}",
					outbox.getId(), outbox.getEventId(), ex);
			return;
		}

		Instant nextAttemptAt = now.plusSeconds(calculateDelaySeconds(nextRetryCount));
		outbox.markRetry(nextAttemptAt, errorMessage);
		log.warn("알림 outbox 재시도 예약. outboxId={} eventId={} retryCount={} nextAttemptAt={}",
				outbox.getId(), outbox.getEventId(), nextRetryCount, nextAttemptAt, ex);
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
