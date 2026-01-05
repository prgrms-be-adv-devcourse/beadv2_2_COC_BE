package com.coc.modi.seller.settlement.batch;

import com.coc.modi.seller.settlement.exception.SettlementPayoutNotReadyException;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

public class SettlementPayoutWriter implements ItemWriter<SettlementPayoutItem> {
	
	@Override
	public void write(Chunk<? extends SettlementPayoutItem> chunk) {
		
		if (chunk == null || chunk.isEmpty()) {
			return;
		}
		
		throw new SettlementPayoutNotReadyException("정산 지급 연동이 아직 준비되지 않았습니다.");
	}
}
