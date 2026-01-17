package com.coc.modi.seller.settlement.application;

import com.coc.modi.seller.settlement.exception.SettlementBatchNotFoundException;
import com.coc.modi.seller.settlement.exception.SellerSettlementConflictException;
import com.coc.modi.seller.settlement.domain.SellerSettlement;
import com.coc.modi.seller.settlement.domain.SellerSettlementLine;
import com.coc.modi.seller.settlement.domain.SellerSettlementRepository;
import com.coc.modi.seller.settlement.domain.SettlementBatch;
import com.coc.modi.seller.settlement.domain.SettlementBatchRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Transactional
public class SettlementAggregationService {
	
	private static final BigDecimal FEE_RATE = BigDecimal.ZERO;
	
	private final SellerSettlementRepository sellerSettlementRepository;
	private final SettlementBatchRepository settlementBatchRepository;
	
	public SellerSettlement aggregateLine(Long batchId,
										  Long sellerId,
										  String periodYm,
										  Long rentalItemId,
										  Long memberId,
										  Long productId,
										  BigDecimal rentalAmount) {
		
		final SettlementBatch batch;
		if (batchId != null) {
			batch = settlementBatchRepository.findById(batchId)
					.orElseThrow(() -> new SettlementBatchNotFoundException("정산 배치를 찾을 수 없습니다. sellerId=" + batchId));
		} else {
			batch = null;
		}
		
		SellerSettlement settlement = sellerSettlementRepository.findBySellerIdAndPeriodYm(sellerId, periodYm)
				.map(existing -> {
					Long existingBatchId = existing.getBatchId();
					if (existingBatchId != null && !existingBatchId.equals(batchId)) {
						throw new SellerSettlementConflictException(
								"정산서는 이미 배치 " + existingBatchId + " 에 의해 생성되었습니다."
						);
					}
					return existing;
				})
				.orElseGet(() -> SellerSettlement.create(batch, sellerId, periodYm));
		
		// TODO: rentalItemId 중복 방지 필요 시 체크
		BigDecimal feeAmount = rentalAmount.multiply(FEE_RATE).setScale(2, RoundingMode.HALF_UP);
		
		SellerSettlementLine line = SellerSettlementLine.of(
				sellerId,
				rentalItemId,
				memberId,
				productId,
				rentalAmount,
				feeAmount
		);
		
		settlement.addLineWithAggregation(line);
		
		return sellerSettlementRepository.save(settlement);
	}
	
}
