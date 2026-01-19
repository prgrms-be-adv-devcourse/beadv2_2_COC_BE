package com.coc.modi.account.wallet.event.outbox;

import com.coc.modi.common.BaseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
		name = "settlement_payout_outbox",
		indexes = {
				@Index(name = "idx_settlement_payout_outbox_status_next", columnList = "status,next_attempt_at")
		}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettlementPayoutOutbox extends BaseEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "event_id", nullable = false, length = 36)
	private String eventId;
	
	@Column(name = "occurred_at", nullable = false)
	private Instant occurredAt;
	
	@Column(name = "settlement_id", nullable = false)
	private Long settlementId;
	
	@Column(name = "seller_id", nullable = false)
	private Long sellerId;
	
	@Column(name = "member_id", nullable = false)
	private Long memberId;
	
	@Column(name = "amount", nullable = false, precision = 18, scale = 2)
	private BigDecimal amount;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "event_type", nullable = false, length = 20)
	private SettlementPayoutOutboxType eventType;
	
	@Column(name = "failure_reason", length = 255)
	private String failureReason;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private SettlementPayoutOutboxStatus status;
	
	@Column(name = "retry_count", nullable = false)
	private int retryCount;
	
	@Column(name = "next_attempt_at")
	private Instant nextAttemptAt;
	
	@Column(name = "last_error", length = 1000)
	private String lastError;
	
	@Builder
	private SettlementPayoutOutbox(
			String eventId,
			Instant occurredAt,
			Long settlementId,
			Long sellerId,
			Long memberId,
			BigDecimal amount,
			SettlementPayoutOutboxType eventType,
			String failureReason,
			SettlementPayoutOutboxStatus status,
			int retryCount,
			Instant nextAttemptAt,
			String lastError
	) {
		
		this.eventId = eventId;
		this.occurredAt = occurredAt;
		this.settlementId = settlementId;
		this.sellerId = sellerId;
		this.memberId = memberId;
		this.amount = amount;
		this.eventType = eventType;
		this.failureReason = failureReason;
		this.status = status != null ? status : SettlementPayoutOutboxStatus.PENDING;
		this.retryCount = Math.max(retryCount, 0);
		this.nextAttemptAt = nextAttemptAt;
		this.lastError = lastError;
	}
	
	public static SettlementPayoutOutbox completed(Long settlementId,
												   Long sellerId,
												   Long memberId,
												   BigDecimal amount) {
		
		return baseBuilder(settlementId, sellerId, memberId, amount)
				.eventType(SettlementPayoutOutboxType.COMPLETED)
				.build();
	}
	
	public static SettlementPayoutOutbox failed(Long settlementId,
												Long sellerId,
												Long memberId,
												BigDecimal amount,
												String failureReason) {
		
		return baseBuilder(settlementId, sellerId, memberId, amount)
				.eventType(SettlementPayoutOutboxType.FAILED)
				.failureReason(failureReason)
				.build();
	}
	
	private static SettlementPayoutOutboxBuilder baseBuilder(Long settlementId,
															 Long sellerId,
															 Long memberId,
															 BigDecimal amount) {
		
		return SettlementPayoutOutbox.builder()
				.eventId(UUID.randomUUID().toString())
				.occurredAt(Instant.now())
				.settlementId(settlementId)
				.sellerId(sellerId)
				.memberId(memberId)
				.amount(amount)
				.status(SettlementPayoutOutboxStatus.PENDING)
				.retryCount(0);
	}
	
	public void markProcessing() {
		
		this.status = SettlementPayoutOutboxStatus.PROCESSING;
	}
	
	public void markSent() {
		
		this.status = SettlementPayoutOutboxStatus.SENT;
		this.nextAttemptAt = null;
		this.lastError = null;
	}
	
	public void markRetry(Instant nextAttemptAt, String errorMessage) {
		
		this.status = SettlementPayoutOutboxStatus.PENDING;
		this.retryCount += 1;
		this.nextAttemptAt = nextAttemptAt;
		this.lastError = errorMessage;
	}
	
	public void markFailed(String errorMessage) {
		
		this.status = SettlementPayoutOutboxStatus.FAILED;
		this.retryCount += 1;
		this.nextAttemptAt = null;
		this.lastError = errorMessage;
	}
}
