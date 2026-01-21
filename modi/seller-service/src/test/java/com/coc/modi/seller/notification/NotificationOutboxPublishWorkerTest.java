package com.coc.modi.seller.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.coc.modi.kafka.event.NotificationEvent;
import com.coc.modi.kafka.topic.KafkaTopics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NotificationOutboxPublishWorkerTest {

	@Mock
	private NotificationOutboxRepository notificationOutboxRepository;

	@Mock
	private KafkaTemplate<String, Object> kafkaTemplate;

	@InjectMocks
	private NotificationOutboxPublishWorker notificationOutboxPublishWorker;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(notificationOutboxPublishWorker, "maxAttempts", 2);
		ReflectionTestUtils.setField(notificationOutboxPublishWorker, "initialDelaySeconds", 5L);
		ReflectionTestUtils.setField(notificationOutboxPublishWorker, "maxDelaySeconds", 300L);
	}

	@Test
	void publishOne_sendsAndMarksSent() {
		NotificationOutbox outbox = notificationOutbox();
		Long outboxId = outbox.getId();

		when(notificationOutboxRepository.claim(eq(outboxId),
				eq(NotificationOutboxStatus.PENDING),
				eq(NotificationOutboxStatus.PROCESSING),
				any()))
				.thenReturn(1);
		when(notificationOutboxRepository.findById(outboxId)).thenReturn(Optional.of(outbox));

		SendResult<String, Object> sendResult = org.mockito.Mockito.mock(SendResult.class);
		when(kafkaTemplate.send(anyString(), anyString(), any()))
				.thenReturn(CompletableFuture.completedFuture(sendResult));

		notificationOutboxPublishWorker.publishOne(outboxId);

		assertThat(outbox.getStatus()).isEqualTo(NotificationOutboxStatus.SENT);
		assertThat(outbox.getLastError()).isNull();
		verify(kafkaTemplate).send(
				eq(KafkaTopics.NOTIFICATION_EVENTS),
				eq(outbox.getReceiverId().toString()),
				any()
		);
	}

	@Test
	void publishOne_onFailure_marksRetry() {
		NotificationOutbox outbox = notificationOutbox();
		Long outboxId = outbox.getId();

		when(notificationOutboxRepository.claim(eq(outboxId),
				eq(NotificationOutboxStatus.PENDING),
				eq(NotificationOutboxStatus.PROCESSING),
				any()))
				.thenReturn(1);
		when(notificationOutboxRepository.findById(outboxId)).thenReturn(Optional.of(outbox));

		CompletableFuture<SendResult<String, Object>> failedFuture = new CompletableFuture<>();
		failedFuture.completeExceptionally(new RuntimeException("boom"));
		when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(failedFuture);

		notificationOutboxPublishWorker.publishOne(outboxId);

		assertThat(outbox.getStatus()).isEqualTo(NotificationOutboxStatus.PENDING);
		assertThat(outbox.getRetryCount()).isEqualTo(1);
		assertThat(outbox.getNextAttemptAt()).isNotNull();
		assertThat(outbox.getLastError()).contains("boom");
	}

	@Test
	void publishOne_skipsWhenNotClaimed() {
		NotificationOutbox outbox = notificationOutbox();
		Long outboxId = outbox.getId();

		when(notificationOutboxRepository.claim(eq(outboxId),
				eq(NotificationOutboxStatus.PENDING),
				eq(NotificationOutboxStatus.PROCESSING),
				any()))
				.thenReturn(0);

		notificationOutboxPublishWorker.publishOne(outboxId);

		verify(notificationOutboxRepository, never()).findById(outboxId);
		verify(kafkaTemplate, never()).send(anyString(), anyString(), any());
	}

	private NotificationOutbox notificationOutbox() {
		NotificationEvent event = NotificationEvent.of(
				100L,
				"SETTLEMENT_PAID",
				"정산이 완료되었습니다",
				"정산금이 지급되었습니다.",
				"SETTLEMENT",
				"1"
		);
		NotificationOutbox outbox = NotificationOutbox.from(event);
		ReflectionTestUtils.setField(outbox, "id", 1L);
		return outbox;
	}
}
