package com.coc.modi.seller.settlement.domain;

import com.coc.modi.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "settlement_batch_execution")
public class SettlementBatchExecution extends BaseEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "batch_type", nullable = false, length = 50)
	private String batchType;
	
	@ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
	@JoinColumn(name = "batch_id")
	private SettlementBatch batch;
	
	@Column(name = "params", columnDefinition = "TEXT")
	private String params;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private SettlementBatchExecutionStatus status;
	
	@Column(name = "started_at")
	private LocalDateTime startedAt;
	
	@Column(name = "ended_at")
	private LocalDateTime endedAt;
	
	@Column(name = "duration_ms")
	private Long durationMs;
	
	@Column(name = "total_count")
	private Integer totalCount;
	
	@Column(name = "success_count")
	private Integer successCount;
	
	@Column(name = "fail_count")
	private Integer failCount;
	
	@Column(name = "total_amount", precision = 18, scale = 2)
	private BigDecimal totalAmount;
	
	@Column(name = "fee_amount", precision = 18, scale = 2)
	private BigDecimal feeAmount;
	
	@Column(name = "last_cursor", length = 100)
	private String lastCursor;
	
	@Column(name = "error_message", length = 500)
	private String errorMessage;
	
	public Long getBatchId() {
		
		return batch != null ? batch.getId() : null;
	}
	
	@Builder
	private SettlementBatchExecution(String batchType,
									 SettlementBatch batch,
									 String params,
									 SettlementBatchExecutionStatus status,
									 LocalDateTime startedAt,
									 LocalDateTime endedAt,
									 Long durationMs,
									 Integer totalCount,
									 Integer successCount,
									 Integer failCount,
									 BigDecimal totalAmount,
									 BigDecimal feeAmount,
									 String lastCursor,
									 String errorMessage) {
		
		this.batchType = batchType;
		this.batch = batch;
		this.params = params;
		this.status = status != null ? status : SettlementBatchExecutionStatus.PENDING;
		this.startedAt = startedAt;
		this.endedAt = endedAt;
		this.durationMs = durationMs;
		this.totalCount = totalCount;
		this.successCount = successCount;
		this.failCount = failCount;
		this.totalAmount = totalAmount;
		this.feeAmount = feeAmount;
		this.lastCursor = lastCursor;
		this.errorMessage = errorMessage;
	}
	
	public static SettlementBatchExecution start(String batchType, SettlementBatch batch, String params) {
		
		return SettlementBatchExecution.builder()
				.batchType(batchType)
				.batch(batch)
				.params(params)
				.status(SettlementBatchExecutionStatus.RUNNING)
				.startedAt(LocalDateTime.now())
				.build();
	}
	
	public void complete(Integer totalCount,
						 Integer successCount,
						 Integer failCount,
						 BigDecimal totalAmount,
						 BigDecimal feeAmount,
						 String lastCursor) {
		
		this.status = SettlementBatchExecutionStatus.COMPLETED;
		this.endedAt = LocalDateTime.now();
		this.durationMs = calculateDuration();
		this.totalCount = totalCount;
		this.successCount = successCount;
		this.failCount = failCount;
		this.totalAmount = totalAmount;
		this.feeAmount = feeAmount;
		this.lastCursor = lastCursor;
	}
	
	public void fail(String errorMessage,
					 Integer totalCount,
					 Integer successCount,
					 Integer failCount,
					 String lastCursor) {
		
		this.status = SettlementBatchExecutionStatus.FAILED;
		this.endedAt = LocalDateTime.now();
		this.durationMs = calculateDuration();
		this.errorMessage = errorMessage;
		this.totalCount = totalCount;
		this.successCount = successCount;
		this.failCount = failCount;
		this.lastCursor = lastCursor;
	}
	
	private Long calculateDuration() {
		
		if (this.startedAt == null || this.endedAt == null) {
			return null;
		}
		return java.time.Duration.between(this.startedAt, this.endedAt).toMillis();
	}
}
