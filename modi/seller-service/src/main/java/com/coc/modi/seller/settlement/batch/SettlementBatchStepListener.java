package com.coc.modi.seller.settlement.batch;

import static com.coc.modi.seller.settlement.batch.SettlementBatchContextKeys.*;

import com.coc.modi.seller.settlement.application.SettlementBatchExecutionLogService;
import com.coc.modi.seller.settlement.domain.SettlementBatchExecutionLogEventType;
import com.coc.modi.seller.settlement.domain.SettlementBatchExecutionLogLevel;
import lombok.RequiredArgsConstructor;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class SettlementBatchStepListener implements StepExecutionListener {
	
	private static final String EXECUTION_ID_KEY = "settlementExecutionId";
	
	private final SettlementBatchExecutionLogService executionLogService;
	
	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		
		long executionId = stepExecution.getJobExecution().getExecutionContext().getLong(EXECUTION_ID_KEY, -1L);
		if (executionId < 0) {
			return stepExecution.getExitStatus();
		}
		ExecutionContext context = stepExecution.getExecutionContext();
		long skipCount = stepExecution.getProcessSkipCount() + stepExecution.getReadSkipCount()
				+ stepExecution.getWriteSkipCount();
		long failureCount = stepExecution.getFailureExceptions().size();
		long failTotal = skipCount + failureCount;
		
		String cursor = context.containsKey(LAST_CURSOR) ? context.getString(LAST_CURSOR) : null;
		BigDecimal totalAmount = getDecimal(context, TOTAL_AMOUNT);
		BigDecimal feeAmount = getDecimal(context, FEE_AMOUNT);
		
		// jobExecutionContext에도 누적 정보를 담아 afterJob에서 합산할 수 있게 전달
		stepExecution.getJobExecution().getExecutionContext().put(TOTAL_AMOUNT, totalAmount);
		stepExecution.getJobExecution().getExecutionContext().put(FEE_AMOUNT, feeAmount);
		stepExecution.getJobExecution().getExecutionContext().put(SKIP_COUNT, skipCount);
		stepExecution.getJobExecution().getExecutionContext().put(FAIL_COUNT, failTotal);
		if (cursor != null) {
			stepExecution.getJobExecution().getExecutionContext().put(LAST_CURSOR, cursor);
		}
		
		executionLogService.append(
				executionId,
				SettlementBatchExecutionLogEventType.STEP,
				SettlementBatchExecutionLogLevel.INFO,
				"Step completed. step=" + stepExecution.getStepName()
						+ ", status=" + stepExecution.getStatus()
						+ ", read=" + stepExecution.getReadCount()
						+ ", write=" + stepExecution.getWriteCount()
						+ ", skip=" + skipCount
						+ ", fail=" + failTotal,
				stepExecution.getStepName()
		);
		
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
