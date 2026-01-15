package com.coc.modi.seller.settlement.domain;

import com.coc.modi.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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
import lombok.ToString;

@Getter
@ToString(of = {"id", "eventType", "level", "stepName", "createdAt"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "settlement_batch_execution_log", schema = "seller")
public class SettlementBatchExecutionLog extends BaseEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "execution_id", nullable = false)
	private SettlementBatchExecution execution;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "event_type", nullable = false, length = 20)
	private SettlementBatchExecutionLogEventType eventType;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "level", nullable = false, length = 10)
	private SettlementBatchExecutionLogLevel level;
	
	@Column(name = "message", columnDefinition = "TEXT", nullable = false)
	private String message;
	
	@Column(name = "step_name", length = 100)
	private String stepName;
	
	public Long getExecutionId() {
		
		return execution != null ? execution.getId() : null;
	}
	
	@Builder
	private SettlementBatchExecutionLog(SettlementBatchExecution execution,
										SettlementBatchExecutionLogEventType eventType,
										SettlementBatchExecutionLogLevel level,
										String message,
										String stepName) {
		
		this.execution = execution;
		this.eventType = eventType;
		this.level = level;
		this.message = message;
		this.stepName = stepName;
	}
	
	public static SettlementBatchExecutionLog of(SettlementBatchExecution execution,
												 SettlementBatchExecutionLogEventType eventType,
												 SettlementBatchExecutionLogLevel level,
												 String message,
												 String stepName) {
		
		return SettlementBatchExecutionLog.builder()
				.execution(execution)
				.eventType(eventType)
				.level(level)
				.message(message)
				.stepName(stepName)
				.build();
	}
}
