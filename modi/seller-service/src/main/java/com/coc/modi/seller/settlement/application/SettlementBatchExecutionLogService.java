package com.coc.modi.seller.settlement.application;

import com.coc.modi.seller.settlement.domain.SettlementBatchExecution;
import com.coc.modi.seller.settlement.domain.SettlementBatchExecutionLog;
import com.coc.modi.seller.settlement.domain.SettlementBatchExecutionLogEventType;
import com.coc.modi.seller.settlement.domain.SettlementBatchExecutionLogLevel;
import com.coc.modi.seller.settlement.domain.SettlementBatchExecutionLogRepository;
import com.coc.modi.seller.settlement.domain.SettlementBatchExecutionRepository;
import com.coc.modi.seller.settlement.exception.SettlementBatchExecutionNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SettlementBatchExecutionLogService {
	
	private final SettlementBatchExecutionRepository executionRepository;
	private final SettlementBatchExecutionLogRepository logRepository;
	
	public SettlementBatchExecutionLog append(Long executionId,
											  SettlementBatchExecutionLogEventType eventType,
											  SettlementBatchExecutionLogLevel level,
											  String message,
											  String stepName) {
		
		SettlementBatchExecution execution = executionRepository.findById(executionId)
				.orElseThrow(() -> new SettlementBatchExecutionNotFoundException(
						"배치 실행을 찾을 수 없습니다. executionId=" + executionId));
		SettlementBatchExecutionLog log = SettlementBatchExecutionLog.of(execution, eventType, level, message, stepName);
		return logRepository.save(log);
	}
}
