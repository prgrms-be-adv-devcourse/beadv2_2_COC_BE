package com.coc.modi.seller.settlement.batch;

import com.coc.modi.seller.application.port.RentalPort;
import com.coc.modi.seller.infrastructure.client.rental.dto.RentalItemInfo;
import com.coc.modi.seller.infrastructure.client.rental.dto.RentalListResponse;
import com.coc.modi.seller.seller.domain.Seller;
import com.coc.modi.seller.seller.domain.SellerRepository;
import com.coc.modi.seller.seller.domain.SellerStatus;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;

import static com.coc.modi.seller.settlement.batch.SettlementBatchContextKeys.LAST_CURSOR;
import static com.coc.modi.seller.settlement.batch.SettlementBatchContextKeys.PAGE;
import static com.coc.modi.seller.settlement.batch.SettlementBatchContextKeys.SELLER_INDEX;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class SettlementRentalItemReader implements ItemStreamReader<RentalItemInfo> {
	
	private static final String STATUS_RETURNED = "RETURNED";
	
	private final RentalPort rentalPort;
	private final SellerRepository sellerRepository;
	private final String periodYm;
	private final String startDate;
	private final String endDate;
	private final Long targetSellerId;
	private final int pageSize;
	
	private List<Long> sellerIds;
	private int sellerIndex = 0;
	private int page = 0;
	private String lastCursor;
	private final Deque<RentalItemInfo> buffer = new ArrayDeque<>();
	
	public SettlementRentalItemReader(RentalPort rentalPort,
									  SellerRepository sellerRepository,
									  String periodYm,
									  String startDate,
									  String endDate,
									  Long targetSellerId,
									  Integer pageSize) {
		
		this.rentalPort = rentalPort;
		this.sellerRepository = sellerRepository;
		this.periodYm = periodYm;
		this.startDate = startDate;
		this.endDate = endDate;
		this.targetSellerId = targetSellerId;
		this.pageSize = pageSize != null && pageSize > 0 ? pageSize : 100;
	}
	
	@Override
	public void open(ExecutionContext executionContext) throws ItemStreamException {
		this.sellerIds = loadSellerIds();
		if (executionContext.containsKey(SELLER_INDEX)) {
			this.sellerIndex = executionContext.getInt(SELLER_INDEX);
		}
		if (executionContext.containsKey(PAGE)) {
			this.page = executionContext.getInt(PAGE);
		}
		if (executionContext.containsKey(LAST_CURSOR)) {
			this.lastCursor = executionContext.getString(LAST_CURSOR);
		}
	}
	
	@Override
	public RentalItemInfo read() {
		
		if (sellerIds == null) {
			this.sellerIds = loadSellerIds();
		}
		
		while (true) {
			if (sellerIds == null || sellerIds.isEmpty() || sellerIndex >= sellerIds.size()) {
				return null;
			}
			
			if (buffer.isEmpty()) {
				Long currentSellerId = sellerIds.get(sellerIndex);
				RentalListResponse response = rentalPort.getRentals(
						currentSellerId,
						STATUS_RETURNED,
						periodYm,
						startDate,
						endDate,
						page,
						pageSize
				);
				List<RentalItemInfo> content = response != null ? response.content() : List.of();
				if (content == null || content.isEmpty()) {
					moveToNextSeller();
					continue;
				}
				buffer.addAll(content);
				this.lastCursor = currentSellerId + ":" + page;
				page++;
			}
			
			RentalItemInfo next = buffer.poll();
			if (next != null) {
				return next;
			}
		}
	}
	
	@Override
	public void update(ExecutionContext executionContext) throws ItemStreamException {
		executionContext.putInt(SELLER_INDEX, sellerIndex);
		executionContext.putInt(PAGE, page);
		executionContext.putString(LAST_CURSOR, lastCursor);
	}
	
	@Override
	public void close() throws ItemStreamException {
		
		buffer.clear();
	}
	
	private List<Long> loadSellerIds() {
		
		if (targetSellerId != null) {
			return sellerRepository.findById(targetSellerId)
					.filter(seller -> seller.getStatus() == SellerStatus.ACTIVE)
					.map(Seller::getId)
					.map(List::of)
					.orElseGet(List::of);
		}
		
		List<Seller> activeSellers = sellerRepository.findByStatus(SellerStatus.ACTIVE);
		List<Long> ids = new ArrayList<>();
		for (Seller seller : activeSellers) {
			if (seller != null && seller.getId() != null) {
				ids.add(seller.getId());
			}
		}
		return ids;
	}
	
	private void moveToNextSeller() {
		
		sellerIndex++;
		page = 0;
		buffer.clear();
	}
}
