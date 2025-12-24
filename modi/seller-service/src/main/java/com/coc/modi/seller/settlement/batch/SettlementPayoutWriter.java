package com.coc.modi.seller.settlement.batch;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

public class SettlementPayoutWriter implements ItemWriter<SettlementPayoutItem> {
	
	@Override
	public void write(Chunk<? extends SettlementPayoutItem> chunk) {
		
		if (chunk == null || chunk.isEmpty()) {
			return;
		}
		
		throw new UnsupportedOperationException("Settlement payout integration is not wired yet.");
	}
}
