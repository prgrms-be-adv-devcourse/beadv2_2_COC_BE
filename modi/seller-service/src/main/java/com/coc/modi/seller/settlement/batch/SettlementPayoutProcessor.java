package com.coc.modi.seller.settlement.batch;

import com.coc.modi.seller.seller.exception.SellerNotFoundException;
import com.coc.modi.seller.seller.domain.Seller;
import com.coc.modi.seller.seller.domain.SellerRepository;
import com.coc.modi.seller.settlement.domain.SellerSettlement;

import org.springframework.batch.item.ItemProcessor;

public class SettlementPayoutProcessor implements ItemProcessor<SellerSettlement, SettlementPayoutItem> {
	
	private final SellerRepository sellerRepository;
	
	public SettlementPayoutProcessor(SellerRepository sellerRepository) {
		
		this.sellerRepository = sellerRepository;
	}
	
	@Override
	public SettlementPayoutItem process(SellerSettlement settlement) {
		
		if (settlement == null) {
			return null;
		}
		
		Long sellerId = settlement.getSellerId();
		Long memberId = sellerRepository.findById(sellerId)
				.map(Seller::getMemberId)
				.orElseThrow(() -> new SellerNotFoundException("판매자를 찾을 수 없습니다. sellerId=" + sellerId));
		
		return new SettlementPayoutItem(
				settlement.getId(),
				sellerId,
				memberId,
				settlement.getSettlementAmount()
		);
	}
}
