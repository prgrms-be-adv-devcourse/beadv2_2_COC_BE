package com.coc.modi.seller.settlement.application;

import com.coc.modi.seller.exception.SettlementBatchExecutionNotFoundException;
import com.coc.modi.seller.settlement.domain.SettlementBatchExecution;
import com.coc.modi.seller.settlement.domain.SettlementBatchExecutionRepository;
import com.coc.modi.seller.settlement.domain.SettlementBatchExecutionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class SettlementBatchExecutionService {

    private final SettlementBatchExecutionRepository executionRepository;

    public SettlementBatchExecution start(String batchType, Long batchId, String params) {
        return executionRepository.save(SettlementBatchExecution.start(batchType, batchId, params));
    }

    public void complete(Long executionId,
                         Integer totalCount,
                         Integer successCount,
                         Integer failCount,
                         BigDecimal totalAmount,
                         BigDecimal feeAmount,
                         String lastCursor) {
        SettlementBatchExecution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new SettlementBatchExecutionNotFoundException("배치 실행을 찾을 수 없습니다. id=" + executionId));
        execution.complete(totalCount, successCount, failCount, totalAmount, feeAmount, lastCursor);
    }

    public void fail(Long executionId,
                     String errorMessage,
                     Integer totalCount,
                     Integer successCount,
                     Integer failCount,
                     String lastCursor) {
        SettlementBatchExecution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new SettlementBatchExecutionNotFoundException("배치 실행을 찾을 수 없습니다. id=" + executionId));
        execution.fail(errorMessage, totalCount, successCount, failCount, lastCursor);
    }
}
