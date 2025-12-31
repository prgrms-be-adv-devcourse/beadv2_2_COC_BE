package com.coc.modi.seller.settlement.batch;

import com.coc.modi.seller.settlement.exception.SettlementInputInvalidException;
import com.coc.modi.seller.settlement.domain.SellerSettlement;
import com.coc.modi.seller.settlement.domain.SellerSettlementStatus;
import com.coc.modi.seller.settlement.infrastructure.SellerSettlementJpaRepository;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;

import java.util.List;

public class SettlementPayoutItemReader implements ItemStreamReader<SellerSettlement> {
	
	private static final String INDEX_KEY = "settlement.payout.index";
	
	private final SellerSettlementJpaRepository settlementRepository;
	private final Long batchId;
	private final SellerSettlementStatus status;
	
	private List<SellerSettlement> items = List.of();
	private int index = 0;
	
	public SettlementPayoutItemReader(SellerSettlementJpaRepository settlementRepository,
									  Long batchId,
									  SellerSettlementStatus status) {
		
		this.settlementRepository = settlementRepository;
		this.batchId = batchId;
		this.status = status;
	}
	
	@Override
	public void open(ExecutionContext executionContext) throws ItemStreamException {
		
		if (batchId == null) {
			throw new SettlementInputInvalidException("batchId is required for payout step");
		}
		this.items = settlementRepository.findByBatch_IdAndStatusOrderByIdAsc(batchId, status);
		if (executionContext.containsKey(INDEX_KEY)) {
			this.index = executionContext.getInt(INDEX_KEY);
		}
	}
	
	@Override
	public SellerSettlement read() {
		
		if (items == null || index >= items.size()) {
			return null;
		}
		return items.get(index++);
	}
	
	@Override
	public void update(ExecutionContext executionContext) throws ItemStreamException {
		
		executionContext.putInt(INDEX_KEY, index);
	}
	
	@Override
	public void close() throws ItemStreamException {
		
		items = List.of();
	}
}
