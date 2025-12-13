package com.coc.modi.seller.settlement.batch;

import com.coc.modi.seller.settlement.application.SettlementAggregationService;

import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemWriter;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.coc.modi.seller.settlement.batch.SettlementBatchContextKeys.FEE_AMOUNT;
import static com.coc.modi.seller.settlement.batch.SettlementBatchContextKeys.SUCCESS_COUNT;
import static com.coc.modi.seller.settlement.batch.SettlementBatchContextKeys.TOTAL_AMOUNT;
import static com.coc.modi.seller.settlement.batch.SettlementBatchContextKeys.TOTAL_COUNT;

public class SettlementAggregationWriter implements ItemWriter<SettlementAggregationItem> {
	
	private static final BigDecimal FEE_RATE = new BigDecimal("0.10");
	private final SettlementAggregationService settlementAggregationService;
	private final Long batchId;
	
	public SettlementAggregationWriter(SettlementAggregationService settlementAggregationService, Long batchId) {
		
		this.settlementAggregationService = settlementAggregationService;
		this.batchId = batchId;
	}
	
	@Override
	public void write(Chunk<? extends SettlementAggregationItem> chunk) {
		
		if (chunk == null || chunk.isEmpty()) {
			return;
		}
		if (StepSynchronizationManager.getContext() == null) {
			return;
		}
		ExecutionContext context = StepSynchronizationManager.getContext().getStepExecution().getExecutionContext();
		for (SettlementAggregationItem item : chunk) {
			if (item == null) {
				continue;
			}
			settlementAggregationService.aggregateLine(
					batchId,
					item.sellerId(),
					item.periodYm(),
					item.rentalItemId(),
					item.memberId(),
					item.productId(),
					item.rentalAmount()
			);
			accumulate(context, item.rentalAmount());
		}
	}
	
	private void accumulate(ExecutionContext context, BigDecimal rentalAmount) {
		
		if (context == null || rentalAmount == null) {
			return;
		}
		
		// 1. 수수료 계산
		BigDecimal fee = rentalAmount.multiply(FEE_RATE).setScale(2, RoundingMode.HALF_UP);
		// 2. 누적: 총 금액 = 현재 금액 + (메모장에 있던 기존 금액)
		context.put(TOTAL_AMOUNT, rentalAmount.add(getDecimal(context, TOTAL_AMOUNT)));
		// 3. 누적: 수수료 총액 = 계산된 수수료 + (메모장에 있던 기존 수수료)
		context.put(FEE_AMOUNT, fee.add(getDecimal(context, FEE_AMOUNT)));
		// 4. 누적: 건수 (+1)
		context.put(TOTAL_COUNT, getLong(context, TOTAL_COUNT) + 1L);
		context.put(SUCCESS_COUNT, getLong(context, SUCCESS_COUNT) + 1L);
	}
	
	private BigDecimal getDecimal(ExecutionContext context, String key) {
		
		Object value = context.get(key);
		if (value instanceof BigDecimal decimal) {
			return decimal;
		}
		return BigDecimal.ZERO;
	}
	
	private Long getLong(ExecutionContext context, String key) {
		
		Object value = context.get(key);
		if (value instanceof Long l) {
			return l;
		}
		if (value instanceof Integer i) {
			return i.longValue();
		}
		return 0L;
	}
}
