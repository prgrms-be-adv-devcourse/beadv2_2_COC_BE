package com.coc.modi.seller.settlement.batch;

import com.coc.modi.seller.settlement.application.SettlementBatchExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;

import static com.coc.modi.seller.settlement.batch.SettlementBatchContextKeys.FEE_AMOUNT;
import static com.coc.modi.seller.settlement.batch.SettlementBatchContextKeys.LAST_CURSOR;
import static com.coc.modi.seller.settlement.batch.SettlementBatchContextKeys.TOTAL_AMOUNT;

@Component
@RequiredArgsConstructor
public class SettlementBatchJobListener implements JobExecutionListener {

    private static final String EXECUTION_ID_KEY = "settlementExecutionId";
    private static final String BATCH_ID_KEY = "batchId";

    private final SettlementBatchExecutionService executionService;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        String batchType = jobExecution.getJobInstance().getJobName();
        String params = jobExecution.getJobParameters().toString();
        Long executionId = executionService.start(batchType, params).getId();
        jobExecution.getExecutionContext().putLong(EXECUTION_ID_KEY, executionId);
        Long batchId = jobExecution.getJobParameters().getLong(BATCH_ID_KEY);
        if (batchId != null) {
            jobExecution.getExecutionContext().putLong(BATCH_ID_KEY, batchId);
        }
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        Long executionId = jobExecution.getExecutionContext().getLong(EXECUTION_ID_KEY, -1L);
        if (executionId < 0) {
            return;
        }

        long readCount = jobExecution.getStepExecutions().stream()
                .mapToLong(se -> se.getReadCount())
                .sum();
        long writeCount = jobExecution.getStepExecutions().stream()
                .mapToLong(se -> se.getWriteCount())
                .sum();
        long skipCount = jobExecution.getStepExecutions().stream()
                .mapToLong(se -> se.getProcessSkipCount() + se.getReadSkipCount() + se.getWriteSkipCount())
                .sum();
        long failureCount = jobExecution.getStepExecutions().stream()
                .mapToLong(se -> se.getFailureExceptions().size())
                .sum();
        long failTotal = skipCount + failureCount;

        BigDecimal totalAmount = jobExecution.getStepExecutions().stream()
                .map(se -> se.getExecutionContext().get(TOTAL_AMOUNT))
                .filter(BigDecimal.class::isInstance)
                .map(BigDecimal.class::cast)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal feeAmount = jobExecution.getStepExecutions().stream()
                .map(se -> se.getExecutionContext().get(FEE_AMOUNT))
                .filter(BigDecimal.class::isInstance)
                .map(BigDecimal.class::cast)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String lastCursor = jobExecution.getStepExecutions().stream()
                .sorted(Comparator.comparingLong(se -> se.getId()))
                .map(se -> {
                    ExecutionContext ctx = se.getExecutionContext();
                    return ctx.containsKey(LAST_CURSOR) ? ctx.getString(LAST_CURSOR) : null;
                })
                .filter(cursor -> cursor != null && !cursor.isBlank())
                .reduce((first, second) -> second)
                .orElse(null);

        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            executionService.complete(
                    executionId,
                    Math.toIntExact(readCount),
                    Math.toIntExact(writeCount),
                    Math.toIntExact(failTotal),
                    totalAmount,
                    feeAmount,
                    lastCursor
            );
        } else {
            String errorMessage = jobExecution.getAllFailureExceptions().stream()
                    .findFirst()
                    .map(Throwable::getMessage)
                    .orElse("Batch failed");
            executionService.fail(
                    executionId,
                    errorMessage,
                    Math.toIntExact(readCount),
                    Math.toIntExact(writeCount),
                    Math.toIntExact(failTotal),
                    lastCursor
            );
        }

        Long batchId = jobExecution.getExecutionContext().containsKey(BATCH_ID_KEY)
                ? jobExecution.getExecutionContext().getLong(BATCH_ID_KEY)
                : null;
        if (batchId != null) {
            // TODO: SettlementBatchService.completeBatch/failBatch 등으로 상태 동기화 추가 필요
        }
    }
}
