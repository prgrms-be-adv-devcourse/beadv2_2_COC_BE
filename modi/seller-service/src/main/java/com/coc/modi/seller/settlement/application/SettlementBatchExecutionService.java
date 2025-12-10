package com.coc.modi.seller.settlement.application;

import com.coc.modi.seller.settlement.domain.SettlementBatchExecution;
import com.coc.modi.seller.settlement.domain.SettlementBatchExecutionLog;
import com.coc.modi.seller.settlement.domain.SettlementBatchExecutionLogRepository;
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
    private final SettlementBatchExecutionLogRepository executionLogRepository;

    public SettlementBatchExecution start(String batchType, String params) {
        return executionRepository.save(SettlementBatchExecution.start(batchType, params));
    }

    public void complete(Long executionId,
                         Integer totalCount,
                         Integer successCount,
                         Integer failCount,
                         BigDecimal totalAmount,
                         BigDecimal feeAmount,
                         String lastCursor) {
        SettlementBatchExecution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new IllegalArgumentException("Execution not found. id=" + executionId));
        execution.complete(totalCount, successCount, failCount, totalAmount, feeAmount, lastCursor);
    }

    public void fail(Long executionId,
                     String errorMessage,
                     Integer totalCount,
                     Integer successCount,
                     Integer failCount,
                     String lastCursor) {
        SettlementBatchExecution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new IllegalArgumentException("Execution not found. id=" + executionId));
        execution.fail(errorMessage, totalCount, successCount, failCount, lastCursor);
    }

    public void log(Long executionId,
                    String stepName,
                    String cursor,
                    Integer processedCount,
                    Integer successCount,
                    Integer failCount,
                    Long durationMs,
                    String errorMessage) {
        SettlementBatchExecution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new IllegalArgumentException("Execution not found. id=" + executionId));
        SettlementBatchExecutionLog log = SettlementBatchExecutionLog.builder()
                .execution(execution)
                .stepName(stepName)
                .cursor(cursor)
                .processedCount(processedCount)
                .successCount(successCount)
                .failCount(failCount)
                .durationMs(durationMs)
                .errorMessage(errorMessage)
                .build();
        executionLogRepository.save(log);
    }
}
