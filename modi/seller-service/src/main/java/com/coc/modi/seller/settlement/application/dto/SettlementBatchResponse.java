package com.coc.modi.seller.settlement.application.dto;

import com.coc.modi.seller.settlement.domain.SettlementBatch;
import com.coc.modi.seller.settlement.domain.SettlementBatchStatus;

import java.time.LocalDateTime;

public record SettlementBatchResponse(
		Long id,
		String periodYm,
		SettlementBatchStatus status,
		LocalDateTime startedAt,
		LocalDateTime completedAt,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {
	
	public static SettlementBatchResponse from(SettlementBatch batch) {
		
		return new SettlementBatchResponse(
				batch.getId(),
				batch.getPeriodYm(),
				batch.getStatus(),
				batch.getStartedAt(),
				batch.getCompletedAt(),
				batch.getCreatedAt(),
				batch.getUpdatedAt()
		);
	}
}
