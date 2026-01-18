package com.coc.modi.seller.notification;

import java.time.Instant;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationOutboxPublisher {

	private final NotificationOutboxRepository notificationOutboxRepository;
	private final NotificationOutboxPublishWorker notificationOutboxPublishWorker;

	@Value("${notification.outbox.publish-batch-size:50}")
	private int publishBatchSize;

	@Scheduled(fixedDelayString = "${notification.outbox.publish-interval-ms:5000}")
	public void publishPending() {

		Instant now = Instant.now();
		List<NotificationOutbox> outboxes = notificationOutboxRepository.findReadyToPublish(
				NotificationOutboxStatus.PENDING,
				now,
				PageRequest.of(0, publishBatchSize)
		);
		for (NotificationOutbox outbox : outboxes) {
			notificationOutboxPublishWorker.publishOne(outbox.getId());
		}
	}
}
