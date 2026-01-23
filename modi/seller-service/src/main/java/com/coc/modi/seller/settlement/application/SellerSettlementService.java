package com.coc.modi.seller.settlement.application;

import com.coc.modi.seller.settlement.exception.SellerSettlementForbiddenException;
import com.coc.modi.seller.settlement.exception.SellerSettlementNotFoundException;
import com.coc.modi.seller.settlement.application.dto.SellerSettlementResponse;
import com.coc.modi.seller.settlement.application.dto.SettlementBulkPayResponse;
import com.coc.modi.seller.settlement.application.dto.SellerSettlementLineResponse;
import com.coc.modi.seller.settlement.domain.SellerSettlement;
import com.coc.modi.seller.settlement.domain.SellerSettlementRepository;
import com.coc.modi.seller.settlement.domain.SellerSettlementStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class SellerSettlementService {
	
	private final SellerSettlementRepository sellerSettlementRepository;
	private final SettlementNotificationService settlementNotificationService;
	private final SettlementPayoutRequestPublisher settlementPayoutRequestPublisher;
	
	public Page<SellerSettlementResponse> getSellerSettlements(Long sellerId, String periodYm, Pageable pageable) {
		
		Page<SellerSettlement> settlements;
		
		if (periodYm != null && !periodYm.isBlank()) {
			settlements = sellerSettlementRepository.findBySellerIdAndPeriodYm(sellerId, periodYm, pageable);
		} else {
			settlements = sellerSettlementRepository.findBySellerId(sellerId, pageable);
		}
		
		return settlements.map(SellerSettlementResponse::from);
	}

	public Page<SellerSettlementResponse> getAllSettlements(String periodYm, Pageable pageable) {

		Page<SellerSettlement> settlements;

		if (periodYm != null && !periodYm.isBlank()) {
			settlements = sellerSettlementRepository.findByPeriodYm(periodYm, pageable);
		} else {
			settlements = sellerSettlementRepository.findAll(pageable);
		}

		return settlements.map(SellerSettlementResponse::from);
	}

	public Page<SellerSettlementResponse> getSettlementsForAdmin(Long sellerId,
																 String periodYm,
																 SellerSettlementStatus status,
																 Pageable pageable) {

		Page<SellerSettlement> settlements = sellerSettlementRepository
				.findByFilter(sellerId, periodYm, status, pageable);

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
	public SellerSettlementResponse requestPayoutByAdmin(Long sellerSettlementId, LocalDateTime requestedAt) {

		SellerSettlement settlement = sellerSettlementRepository.findById(sellerSettlementId)
				.orElseThrow(() -> new SellerSettlementNotFoundException(
						"정산서를 찾을 수 없습니다. sellerSettlementId=" + sellerSettlementId
				));
		return processPayoutRequest(settlement, requestedAt);
	}

	@Transactional
	public SettlementBulkPayResponse requestPayoutsByAdmin(Long sellerId,
														   String periodYm,
														   SellerSettlementStatus status,
														   LocalDateTime requestedAt) {

		List<SellerSettlement> settlements = sellerSettlementRepository
				.findListByFilter(sellerId, periodYm, status);

		int total = settlements.size();
		int requested = 0;
		int skipped = 0;

		for (SellerSettlement settlement : settlements) {
			try {
				processPayoutRequest(settlement, requestedAt);
				requested += 1;
			} catch (RuntimeException ex) {
				skipped += 1;
				log.warn("정산 지급 일괄 처리에서 제외되었습니다. settlementId={} status={}",
						settlement.getId(), settlement.getStatus(), ex);
			}
		}

		return new SettlementBulkPayResponse(total, requested, skipped);
	}

	private SellerSettlementResponse processPayoutRequest(SellerSettlement settlement, LocalDateTime requestedAt) {

		if (settlement.getSettlementAmount() == null || settlement.getSettlementAmount().signum() <= 0) {
			LocalDateTime paidAt = requestedAt != null ? requestedAt : LocalDateTime.now();
			settlement.pay(paidAt);
			settlementNotificationService.notifySettlementPaid(settlement);
			return SellerSettlementResponse.from(settlement);
		}

		settlement.requestPayout();
		sellerSettlementRepository.save(settlement);
		settlementPayoutRequestPublisher.publish(settlement);
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
