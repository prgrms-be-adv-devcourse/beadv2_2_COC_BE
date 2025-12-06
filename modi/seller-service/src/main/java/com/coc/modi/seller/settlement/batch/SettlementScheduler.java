package com.coc.modi.seller.settlement.batch;

import com.coc.modi.seller.settlement.application.SettlementAggregationService;
import com.coc.modi.seller.settlement.application.SettlementBatchService;
import com.coc.modi.seller.settlement.application.dto.SettlementBatchCreateCommand;
import com.coc.modi.seller.settlement.application.dto.SettlementBatchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
@Slf4j
public class SettlementScheduler {

    private static final DateTimeFormatter PERIOD_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final SettlementBatchService settlementBatchService;
    private final SettlementAggregationService settlementAggregationService;

    @Scheduled(cron = "0 5 0 1 * ?")
    public void runMonthlySettlement() {
        String periodYm = PERIOD_FORMATTER.format(LocalDate.now().minusMonths(1));
        try {
            SettlementBatchResponse batch = settlementBatchService.createBatch(new SettlementBatchCreateCommand(periodYm));
            settlementBatchService.startBatch(batch.id());
            // TODO: 외부 데이터 조회 후 aggregateFromRental 호출로 집계 실행
            settlementBatchService.completeBatch(batch.id());
            log.info("Settlement batch completed. periodYm={}", periodYm);
        } catch (Exception e) {
            log.error("Settlement batch failed. periodYm={}", periodYm, e);
        }
    }
}
