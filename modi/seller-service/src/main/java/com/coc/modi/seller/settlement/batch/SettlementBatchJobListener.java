package com.coc.modi.seller.settlement.batch;

import com.coc.modi.seller.settlement.application.SettlementBatchExecutionService;
import com.coc.modi.seller.settlement.domain.SettlementBatchExecutionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SettlementBatchJobListener implements JobExecutionListener {

    private static final String EXECUTION_ID_KEY = "settlementExecutionId";

    private final SettlementBatchExecutionService executionService;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        String batchType = jobExecution.getJobInstance().getJobName();
        String params = jobExecution.getJobParameters().toString();
        Long executionId = executionService.start(batchType, params).getId();
        jobExecution.getExecutionContext().putLong(EXECUTION_ID_KEY, executionId);
    }

//    @Override
//    public void afterJob(JobExecution jobExecution) {
//        Long executionId = jobExecution.getExecutionContext().getLong(EXECUTION_ID_KEY, -1L);
//        if (executionId < 0) {
//            return;
//        }
//
//        int readCount = jobExecution.getStepExecutions().stream()
//                .mapToInt(se -> se.getReadCount())
//                .sum();
//        int writeCount = jobExecution.getStepExecutions().stream()
//                .mapToInt(se -> se.getWriteCount())
//                .sum();
//        int processSkip = jobExecution.getStepExecutions().stream()
//                .mapToInt(se -> se.getProcessSkipCount() + se.getReadSkipCount() + se.getWriteSkipCount())
//                .sum();
//
//        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
//            executionService.complete(executionId, readCount, writeCount, processSkip, null, null, null);
//        } else {
//            String errorMessage = jobExecution.getAllFailureExceptions().stream()
//                    .findFirst()
//                    .map(Throwable::getMessage)
//                    .orElse("Batch failed");
//            executionService.fail(executionId, errorMessage, readCount, writeCount, processSkip, null);
//        }
//    }
}
