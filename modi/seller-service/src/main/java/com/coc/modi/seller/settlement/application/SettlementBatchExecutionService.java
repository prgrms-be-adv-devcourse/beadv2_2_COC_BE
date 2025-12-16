package com.coc.modi.seller.settlement.application;

import com.coc.modi.seller.exception.SettlementBatchExecutionNotFoundException;
import com.coc.modi.seller.exception.SettlementBatchNotFoundException;
import com.coc.modi.seller.settlement.domain.SettlementBatchExecution;
import com.coc.modi.seller.settlement.domain.SettlementBatchExecutionRepository;
import com.coc.modi.seller.settlement.domain.SettlementBatch;
import com.coc.modi.seller.settlement.domain.SettlementBatchRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class SettlementBatchExecutionService {
	
	private final SettlementBatchExecutionRepository executionRepository;
	private final SettlementBatchRepository settlementBatchRepository;
	
	public SettlementBatchExecution start(String batchType, Long batchId, String params) {
		
		SettlementBatch batch = null;
		if (batchId != null) {
			batch = settlementBatchRepository.findById(batchId)
					.orElseThrow(() -> new SettlementBatchNotFoundException("정산 배치를 찾을 수 없습니다. sellerId=" + batchId));
		}
		return executionRepository.save(SettlementBatchExecution.start(batchType, batch, params));
	}
	
	public void complete(Long executionId,
						 Integer totalCount,
						 Integer successCount,
						 Integer failCount,
						 BigDecimal totalAmount,
						 BigDecimal feeAmount,
						 String lastCursor) {
		
		SettlementBatchExecution execution = executionRepository.findById(executionId)
				.orElseThrow(() -> new SettlementBatchExecutionNotFoundException(
						"배치 실행을 찾을 수 없습니다. sellerId=" + executionId));
		execution.complete(totalCount, successCount, failCount, totalAmount, feeAmount, lastCursor);
	}
	
	public void fail(Long executionId,
					 String errorMessage,
					 Integer totalCount,
					 Integer successCount,
					 Integer failCount,
					 String lastCursor) {
		
		SettlementBatchExecution execution = executionRepository.findById(executionId)
				.orElseThrow(() -> new SettlementBatchExecutionNotFoundException(
						"배치 실행을 찾을 수 없습니다. sellerId=" + executionId));
		execution.fail(errorMessage, totalCount, successCount, failCount, lastCursor);
	}
}
