package com.coc.modi.seller.settlement.batch;

import com.coc.modi.seller.settlement.application.SettlementBatchTriggerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(value = "settlement.scheduler.enabled", matchIfMissing = true)
public class SettlementScheduler {
	
	private final SettlementBatchTriggerService settlementBatchTriggerService;
	
	@Scheduled(cron = "${settlement.scheduler.cron:0 5 0 1 * ?}")
	public void runMonthlySettlement() {
		
		try {
			settlementBatchTriggerService.runMonthly();
		} catch (Exception e) {
			log.error("Settlement monthly batch trigger failed.", e);
		}
	}
}
