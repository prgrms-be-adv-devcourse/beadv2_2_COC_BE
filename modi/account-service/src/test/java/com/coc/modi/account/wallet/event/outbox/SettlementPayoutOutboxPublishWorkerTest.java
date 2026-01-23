package com.coc.modi.account.wallet.event.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

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
class SettlementPayoutOutboxPublishWorkerTest {

	@Mock
	private SettlementPayoutOutboxRepository settlementPayoutOutboxRepository;

	@Mock
	private KafkaTemplate<String, Object> kafkaTemplate;

	@InjectMocks
	private SettlementPayoutOutboxPublishWorker settlementPayoutOutboxPublishWorker;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(settlementPayoutOutboxPublishWorker, "maxAttempts", 2);
		ReflectionTestUtils.setField(settlementPayoutOutboxPublishWorker, "initialDelaySeconds", 5L);
		ReflectionTestUtils.setField(settlementPayoutOutboxPublishWorker, "maxDelaySeconds", 300L);
	}

	@Test
	void publishOne_sendsCompletedAndMarksSent() {
		SettlementPayoutOutbox outbox = completedOutbox();
		Long outboxId = outbox.getId();

		when(settlementPayoutOutboxRepository.claim(eq(outboxId),
				eq(SettlementPayoutOutboxStatus.PENDING),
				eq(SettlementPayoutOutboxStatus.PROCESSING),
				any()))
				.thenReturn(1);
		when(settlementPayoutOutboxRepository.findById(outboxId)).thenReturn(Optional.of(outbox));

		SendResult<String, Object> sendResult = org.mockito.Mockito.mock(SendResult.class);
		when(kafkaTemplate.send(anyString(), anyString(), any()))
				.thenReturn(CompletableFuture.completedFuture(sendResult));

		settlementPayoutOutboxPublishWorker.publishOne(outboxId);

		assertThat(outbox.getStatus()).isEqualTo(SettlementPayoutOutboxStatus.SENT);
		verify(kafkaTemplate).send(
				eq(KafkaTopics.ACCOUNT_SETTLEMENT_PAYOUT_COMPLETED),
				eq(outbox.getSettlementId().toString()),
				any()
		);
	}

	@Test
	void publishOne_sendsFailedAndMarksSent() {
		SettlementPayoutOutbox outbox = failedOutbox();
		Long outboxId = outbox.getId();

		when(settlementPayoutOutboxRepository.claim(eq(outboxId),
				eq(SettlementPayoutOutboxStatus.PENDING),
				eq(SettlementPayoutOutboxStatus.PROCESSING),
				any()))
				.thenReturn(1);
		when(settlementPayoutOutboxRepository.findById(outboxId)).thenReturn(Optional.of(outbox));

		SendResult<String, Object> sendResult = org.mockito.Mockito.mock(SendResult.class);
		when(kafkaTemplate.send(anyString(), anyString(), any()))
				.thenReturn(CompletableFuture.completedFuture(sendResult));

		settlementPayoutOutboxPublishWorker.publishOne(outboxId);

		assertThat(outbox.getStatus()).isEqualTo(SettlementPayoutOutboxStatus.SENT);
		verify(kafkaTemplate).send(
				eq(KafkaTopics.ACCOUNT_SETTLEMENT_PAYOUT_FAILED),
				eq(outbox.getSettlementId().toString()),
				any()
		);
	}

	@Test
	void publishOne_onFailure_marksRetry() {
		SettlementPayoutOutbox outbox = completedOutbox();
		Long outboxId = outbox.getId();

		when(settlementPayoutOutboxRepository.claim(eq(outboxId),
				eq(SettlementPayoutOutboxStatus.PENDING),
				eq(SettlementPayoutOutboxStatus.PROCESSING),
				any()))
				.thenReturn(1);
		when(settlementPayoutOutboxRepository.findById(outboxId)).thenReturn(Optional.of(outbox));

		CompletableFuture<SendResult<String, Object>> failedFuture = new CompletableFuture<>();
		failedFuture.completeExceptionally(new RuntimeException("boom"));
		when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(failedFuture);

		settlementPayoutOutboxPublishWorker.publishOne(outboxId);

		assertThat(outbox.getStatus()).isEqualTo(SettlementPayoutOutboxStatus.PENDING);
		assertThat(outbox.getRetryCount()).isEqualTo(1);
		assertThat(outbox.getNextAttemptAt()).isNotNull();
		assertThat(outbox.getLastError()).contains("boom");
	}

	@Test
	void publishOne_skipsWhenNotClaimed() {
		SettlementPayoutOutbox outbox = completedOutbox();
		Long outboxId = outbox.getId();

		when(settlementPayoutOutboxRepository.claim(eq(outboxId),
				eq(SettlementPayoutOutboxStatus.PENDING),
				eq(SettlementPayoutOutboxStatus.PROCESSING),
				any()))
				.thenReturn(0);

		settlementPayoutOutboxPublishWorker.publishOne(outboxId);

		verify(settlementPayoutOutboxRepository, never()).findById(outboxId);
		verify(kafkaTemplate, never()).send(anyString(), anyString(), any());
	}

	private SettlementPayoutOutbox completedOutbox() {
		SettlementPayoutOutbox outbox = SettlementPayoutOutbox.completed(
				1L,
				10L,
				100L,
				new BigDecimal("9000")
		);
		ReflectionTestUtils.setField(outbox, "id", 1L);
		return outbox;
	}

	private SettlementPayoutOutbox failedOutbox() {
		SettlementPayoutOutbox outbox = SettlementPayoutOutbox.failed(
				2L,
				11L,
				101L,
				new BigDecimal("4500"),
				"지급 실패"
		);
		ReflectionTestUtils.setField(outbox, "id", 2L);
		return outbox;
	}
}
