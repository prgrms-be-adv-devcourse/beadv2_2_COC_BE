package com.coc.modi.seller.settlement.application;

import com.coc.modi.seller.settlement.application.dto.SettlementBatchCreateCommand;
import com.coc.modi.seller.settlement.application.dto.SettlementBatchResponse;
import com.coc.modi.seller.settlement.application.dto.SettlementBatchRunCommand;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementBatchTriggerService {
	
	private static final DateTimeFormatter PERIOD_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
	
	private final SettlementBatchService settlementBatchService;
	private final SettlementBatchRunner settlementBatchRunner;
	
	public SettlementBatchResponse runMonthly() {
		
		return runMonthly(YearMonth.now().minusMonths(1));
	}
	
	public SettlementBatchResponse runMonthly(YearMonth targetMonth) {
		
		String periodYm = targetMonth.format(PERIOD_FORMATTER);
		String startDate = targetMonth.atDay(1).toString();
		String endDate = targetMonth.atEndOfMonth().toString();
		
		SettlementBatchResponse batch = settlementBatchService.createBatch(new SettlementBatchCreateCommand(periodYm));
		
		try {
			log.info("Settlement batch run started. batchId={}, periodYm={}", batch.id(), periodYm);
			settlementBatchRunner.run(batch.id(), new SettlementBatchRunCommand(
					periodYm,
					startDate,
					endDate,
					null,
					null
			));
			return settlementBatchService.getBatch(batch.id());
		} catch (Exception e) {
			log.error("Settlement batch run failed. batchId={}, periodYm={}", batch.id(), periodYm, e);
			throw e;
		}
	}
}
