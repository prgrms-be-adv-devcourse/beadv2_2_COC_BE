package com.coc.modi.seller.settlement.application;

import com.coc.modi.seller.exception.SettlementBatchRunException;
import com.coc.modi.seller.exception.SettlementInputInvalidException;
import com.coc.modi.seller.settlement.application.dto.SettlementBatchRunCommand;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Service
@Slf4j
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
					log.warn("정산 배치 실패 상태 전환 중 오류가 발생했습니다. batchId={}", batchId, ignored);
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
		LocalDate start = parseDate(command.startDate(), "startDate");
		LocalDate end = parseDate(command.endDate(), "endDate");
		if (start.isAfter(end)) {
			throw new SettlementInputInvalidException("startDate는 endDate보다 이후일 수 없습니다.");
		}
	}
	
	private boolean isBlank(String value) {
		
		return value == null || value.isBlank();
	}

	private LocalDate parseDate(String value, String fieldName) {
		
		try {
			return LocalDate.parse(value);
		} catch (DateTimeParseException e) {
			throw new SettlementInputInvalidException(fieldName + " 형식이 올바르지 않습니다. (yyyy-MM-dd)", e);
		}
	}
}
