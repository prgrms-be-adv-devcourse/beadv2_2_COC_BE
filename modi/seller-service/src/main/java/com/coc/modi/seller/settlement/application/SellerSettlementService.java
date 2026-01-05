package com.coc.modi.seller.settlement.application;

import com.coc.modi.seller.settlement.exception.SellerSettlementForbiddenException;
import com.coc.modi.seller.settlement.exception.SellerSettlementNotFoundException;
import com.coc.modi.seller.settlement.application.dto.SellerSettlementResponse;
import com.coc.modi.seller.settlement.application.dto.SellerSettlementLineResponse;
import com.coc.modi.seller.settlement.domain.SellerSettlement;
import com.coc.modi.seller.settlement.domain.SellerSettlementRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerSettlementService {
	
	private final SellerSettlementRepository sellerSettlementRepository;
	private final SettlementNotificationService settlementNotificationService;
	
	public Page<SellerSettlementResponse> getSellerSettlements(Long sellerId, String periodYm, Pageable pageable) {
		
		Page<SellerSettlement> settlements;
		
		if (periodYm != null && !periodYm.isBlank()) {
			settlements = sellerSettlementRepository.findBySellerIdAndPeriodYm(sellerId, periodYm, pageable);
		} else {
			settlements = sellerSettlementRepository.findBySellerId(sellerId, pageable);
		}
		
		return settlements.map(SellerSettlementResponse::from);
	}
	
	public SellerSettlementResponse getSellerSettlement(Long sellerId, Long sellerSettlementId) {
		
		SellerSettlement settlement = findOwnedSettlement(sellerId, sellerSettlementId);
		return SellerSettlementResponse.from(settlement);
	}
	
	public List<SellerSettlementLineResponse> getSettlementLines(Long sellerId, Long sellerSettlementId) {
		
		SellerSettlement settlement = findOwnedSettlement(sellerId, sellerSettlementId);
		return settlement.getLines().stream()
				.map(SellerSettlementLineResponse::from)
				.toList();
	}
	
	@Transactional
	public SellerSettlementResponse markAsPaid(Long sellerId, Long sellerSettlementId, LocalDateTime paidAt) {
		
		SellerSettlement settlement = findOwnedSettlement(sellerId, sellerSettlementId);
		settlement.pay(paidAt);
		settlementNotificationService.notifySettlementPaid(settlement);
		return SellerSettlementResponse.from(settlement);
	}
	
	@Transactional
	public SellerSettlementResponse cancelSettlement(Long sellerId, Long sellerSettlementId) {
		
		SellerSettlement settlement = findOwnedSettlement(sellerId, sellerSettlementId);
		settlement.cancel();
		return SellerSettlementResponse.from(settlement);
	}
	
	private SellerSettlement findOwnedSettlement(Long sellerId, Long sellerSettlementId) {
		
		SellerSettlement settlement = sellerSettlementRepository.findById(sellerSettlementId)
				.orElseThrow(() -> new SellerSettlementNotFoundException("정산서를 찾을 수 없습니다. sellerId=" + sellerSettlementId));
		
		if (!settlement.getSellerId().equals(sellerId)) {
			throw new SellerSettlementForbiddenException("정산서 소유자가 일치하지 않습니다. sellerId=" + sellerId);
		}
		return settlement;
	}
}
