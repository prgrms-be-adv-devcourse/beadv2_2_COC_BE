package com.coc.modi.seller.settlement.application;

import com.coc.modi.seller.settlement.exception.SettlementBatchNotFoundException;
import com.coc.modi.seller.settlement.exception.SellerSettlementConflictException;
import com.coc.modi.seller.settlement.domain.SellerSettlement;
import com.coc.modi.seller.settlement.domain.SellerSettlementRepository;
import com.coc.modi.seller.settlement.domain.SellerSettlementStatus;
import com.coc.modi.seller.settlement.domain.SettlementBatch;
import com.coc.modi.seller.settlement.domain.SettlementBatchRepository;
import com.coc.modi.seller.settlement.infrastructure.SellerSettlementLineJdbcRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class SettlementAggregationService {
	
	private static final BigDecimal FEE_RATE = BigDecimal.ZERO;
	
	private final SellerSettlementRepository sellerSettlementRepository;
	private final SettlementBatchRepository settlementBatchRepository;
	private final SellerSettlementLineJdbcRepository sellerSettlementLineJdbcRepository;
	
	public boolean aggregateLine(Long batchId,
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
				.orElseGet(() -> sellerSettlementRepository.save(SellerSettlement.create(batch, sellerId, periodYm)));
		
		BigDecimal feeAmount = rentalAmount.multiply(FEE_RATE).setScale(2, RoundingMode.HALF_UP);

		return sellerSettlementLineJdbcRepository.insertLineAndAccumulate(
				settlement.getId(),
				sellerId,
				rentalItemId,
				memberId,
				productId,
				rentalAmount,
				feeAmount
		);
	}

	public boolean cancelLine(Long sellerId,
							  String periodYm,
							  Long rentalItemId,
							  LocalDateTime canceledAt) {

		if (sellerId == null || periodYm == null || periodYm.isBlank() || rentalItemId == null) {
			return false;
		}

		SellerSettlement settlement = sellerSettlementRepository.findBySellerIdAndPeriodYm(sellerId, periodYm)
				.orElse(null);
		if (settlement == null) {
			return false;
		}
		if (settlement.getStatus() == SellerSettlementStatus.PAID) {
			return false;
		}

		return sellerSettlementLineJdbcRepository.cancelLineAndAdjust(
				settlement.getId(),
				rentalItemId,
				canceledAt
		);
	}
	
}
