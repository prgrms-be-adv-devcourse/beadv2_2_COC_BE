package com.coc.modi.seller.settlement.batch;

import com.coc.modi.seller.settlement.application.SettlementBatchExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

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

//    @Override
//    public ExitStatus afterStep(StepExecution stepExecution) {
//        Long executionId = stepExecution.getJobExecution().getExecutionContext().getLong(EXECUTION_ID_KEY, -1L);
//        if (executionId < 0) {
//            return stepExecution.getExitStatus();
//        }
//        Long startedAt = stepExecution.getExecutionContext().getLong(STEP_STARTED_AT, System.currentTimeMillis());
//        long duration = System.currentTimeMillis() - startedAt;
//
//        int readCount = stepExecution.getReadCount();
//        int writeCount = stepExecution.getWriteCount();
//        int skipCount = stepExecution.getProcessSkipCount() + stepExecution.getReadSkipCount() + stepExecution.getWriteSkipCount();
//
//        executionService.log(
//                executionId,
//                stepExecution.getStepName(),
//                null,
//                readCount,
//                writeCount,
//                skipCount,
//                duration,
//                stepExecution.getExitStatus().getExitDescription()
//        );
//        return stepExecution.getExitStatus();
//    }
}
