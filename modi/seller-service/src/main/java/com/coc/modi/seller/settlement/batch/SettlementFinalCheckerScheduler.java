package com.coc.modi.seller.settlement.batch;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import com.coc.modi.seller.settlement.application.SettlementFinalCheckerService;

import java.time.YearMonth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(value = "settlement.final-checker.enabled", matchIfMissing = true)
public class SettlementFinalCheckerScheduler {
	private final SettlementFinalCheckerService finalCheckerService;

	@Value("${settlement.final-checker.page-size:200}")
	private int pageSize;

	@Scheduled(cron = "${settlement.final-checker.cron:0 5 0 1 * ?}")
	public void runFinalCheck() {

		YearMonth targetMonth = YearMonth.now().minusMonths(1);
		try {
			log.info("Settlement final check started. periodYm={}", targetMonth);
			finalCheckerService.runFinalCheck(targetMonth, pageSize);
		} catch (Exception e) {
			log.error("Settlement final check failed. periodYm={}", targetMonth, e);
		}
	}
}
