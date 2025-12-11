package com.coc.modi.seller.settlement.batch;

import com.coc.modi.seller.settlement.application.SettlementAggregationService;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

public class SettlementAggregationWriter implements ItemWriter<SettlementAggregationItem> {
	
	private final SettlementAggregationService settlementAggregationService;
	
	public SettlementAggregationWriter(SettlementAggregationService settlementAggregationService) {
		
		this.settlementAggregationService = settlementAggregationService;
	}
	
	@Override
	public void write(Chunk<? extends SettlementAggregationItem> chunk) {
		
		if (chunk == null || chunk.isEmpty()) {
			return;
		}
		for (SettlementAggregationItem item : chunk) {
			if (item == null) {
				continue;
			}
			settlementAggregationService.aggregateLine(
					item.sellerId(),
					item.periodYm(),
					item.rentalItemId(),
					item.memberId(),
					item.productId(),
					item.rentalAmount()
			);
		}
	}
}
