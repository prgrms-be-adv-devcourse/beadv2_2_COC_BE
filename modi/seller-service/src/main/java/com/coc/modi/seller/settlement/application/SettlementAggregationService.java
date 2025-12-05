package com.coc.modi.seller.settlement.application;

import com.coc.modi.seller.settlement.domain.SellerSettlement;
import com.coc.modi.seller.settlement.domain.SellerSettlementLine;
import com.coc.modi.seller.settlement.domain.SellerSettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class SettlementAggregationService {

    private static final BigDecimal FEE_RATE = new BigDecimal("0.10");

    private final SellerSettlementRepository sellerSettlementRepository;

    public SellerSettlement aggregateLine(Long sellerId,
                                          String periodYm,
                                          Long rentalId,
                                          Long memberId,
                                          Long productId,
                                          BigDecimal rentalAmount) {
        SellerSettlement settlement = sellerSettlementRepository.findBySellerIdAndPeriodYm(sellerId, periodYm)
                .orElseGet(() -> SellerSettlement.create(null, sellerId, periodYm));

        BigDecimal feeAmount = rentalAmount.multiply(FEE_RATE).setScale(2, BigDecimal.ROUND_HALF_UP);

        SellerSettlementLine line = SellerSettlementLine.of(
                sellerId,
                rentalId,
                memberId,
                productId,
                rentalAmount,
                feeAmount
        );

        settlement.addLineWithAggregation(line);

        return sellerSettlementRepository.save(settlement);
    }
}
