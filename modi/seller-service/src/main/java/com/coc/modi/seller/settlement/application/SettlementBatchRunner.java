package com.coc.modi.seller.settlement.application;

import com.coc.modi.seller.exception.SettlementBatchRunException;
import com.coc.modi.seller.exception.SettlementInputInvalidException;
import com.coc.modi.seller.settlement.application.dto.SettlementBatchRunCommand;

import lombok.RequiredArgsConstructor;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettlementBatchRunner {
	
	private final JobLauncher jobLauncher;
	private final Job settlementAggregationJob;
	private final SettlementBatchService settlementBatchService;
	
	public void run(Long batchId, SettlementBatchRunCommand command) {
		
		validate(command);
		
		if (batchId != null) {
			settlementBatchService.startBatch(batchId);
		}
		
		JobParametersBuilder params = new JobParametersBuilder()
				.addLong("timestamp", System.currentTimeMillis())
				.addString("periodYm", command.periodYm())
				.addString("startDate", command.startDate())
				.addString("endDate", command.endDate());
		
		if (batchId != null) {
			params.addLong("batchId", batchId);
		}
		if (command.sellerId() != null) {
			params.addLong("sellerId", command.sellerId());
		}
		if (command.pageSize() != null && command.pageSize() > 0) {
			params.addLong("pageSize", command.pageSize().longValue());
		}
		
		try {
			jobLauncher.run(settlementAggregationJob, params.toJobParameters());
		} catch (Exception e) {
			if (batchId != null) {
				try {
					settlementBatchService.failBatch(batchId);
				} catch (Exception ignored) {
					// 상태 롤백 중 실패는 무시
				}
			}
			throw new SettlementBatchRunException("정산 배치 실행에 실패했습니다.", e);
		}
	}
	
	private void validate(SettlementBatchRunCommand command) {
		
		if (command == null) {
			throw new SettlementInputInvalidException("요청 본문이 비어 있습니다.");
		}
		if (isBlank(command.periodYm())) {
			throw new SettlementInputInvalidException("periodYm은 필수입니다.");
		}
		if (isBlank(command.startDate()) || isBlank(command.endDate())) {
			throw new SettlementInputInvalidException("startDate와 endDate는 필수입니다.");
		}
	}
	
	private boolean isBlank(String value) {
		
		return value == null || value.isBlank();
	}
}
