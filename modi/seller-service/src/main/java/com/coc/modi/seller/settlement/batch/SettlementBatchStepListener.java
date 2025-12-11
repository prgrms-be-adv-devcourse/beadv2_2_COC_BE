package com.coc.modi.seller.settlement.batch;

import com.coc.modi.seller.settlement.application.SettlementBatchExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import static com.coc.modi.seller.settlement.batch.SettlementBatchContextKeys.FEE_AMOUNT;
import static com.coc.modi.seller.settlement.batch.SettlementBatchContextKeys.LAST_CURSOR;
import static com.coc.modi.seller.settlement.batch.SettlementBatchContextKeys.TOTAL_AMOUNT;

@Component
@RequiredArgsConstructor
public class SettlementBatchStepListener implements StepExecutionListener {

    private static final String EXECUTION_ID_KEY = "settlementExecutionId";
    private static final String STEP_STARTED_AT = "stepStartedAt";

    private final SettlementBatchExecutionService executionService;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        stepExecution.getExecutionContext().putLong(STEP_STARTED_AT, System.currentTimeMillis());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        Long executionId = stepExecution.getJobExecution().getExecutionContext().getLong(EXECUTION_ID_KEY, -1L);
        if (executionId < 0) {
            return stepExecution.getExitStatus();
        }
        ExecutionContext context = stepExecution.getExecutionContext();
        Long startedAt = context.getLong(STEP_STARTED_AT, System.currentTimeMillis());
        long duration = System.currentTimeMillis() - startedAt;

        int readCount = stepExecution.getReadCount();
        int writeCount = stepExecution.getWriteCount();
        int skipCount = stepExecution.getProcessSkipCount() + stepExecution.getReadSkipCount() + stepExecution.getWriteSkipCount();
        int failCount = stepExecution.getFailureExceptions().size();

        String cursor = context.containsKey(LAST_CURSOR) ? context.getString(LAST_CURSOR) : null;
        BigDecimal totalAmount = getDecimal(context, TOTAL_AMOUNT);
        BigDecimal feeAmount = getDecimal(context, FEE_AMOUNT);

        executionService.log(
                executionId,
                stepExecution.getStepName(),
                cursor,
                readCount,
                writeCount,
                skipCount,
                duration,
                stepExecution.getExitStatus().getExitDescription()
        );

        // jobExecutionContext에도 누적 정보를 담아 afterJob에서 합산할 수 있게 전달
        stepExecution.getJobExecution().getExecutionContext().put(TOTAL_AMOUNT, totalAmount);
        stepExecution.getJobExecution().getExecutionContext().put(FEE_AMOUNT, feeAmount);
        stepExecution.getJobExecution().getExecutionContext().put(SKIP_COUNT, skipCount);
        stepExecution.getJobExecution().getExecutionContext().put(FAIL_COUNT, failCount);
        if (cursor != null) {
            stepExecution.getJobExecution().getExecutionContext().put(LAST_CURSOR, cursor);
        }

        return stepExecution.getExitStatus();
    }

    private BigDecimal getDecimal(ExecutionContext context, String key) {
        Object value = context.get(key);
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        return BigDecimal.ZERO;
    }
}
