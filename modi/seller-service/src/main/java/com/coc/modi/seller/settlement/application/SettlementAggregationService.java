package com.coc.modi.seller.settlement.application;

import com.coc.modi.seller.settlement.domain.SellerSettlement;
import com.coc.modi.seller.settlement.domain.SellerSettlementLine;
import com.coc.modi.seller.settlement.domain.SellerSettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Transactional
public class SettlementAggregationService {

    private static final BigDecimal FEE_RATE = new BigDecimal("0.10");

    private final SellerSettlementRepository sellerSettlementRepository;

    public SellerSettlement aggregateLine(Long sellerId,
                                          String periodYm,
                                          Long rentalItemId,
                                          Long memberId,
                                          Long productId,
                                          BigDecimal rentalAmount) {
        SellerSettlement settlement = sellerSettlementRepository.findBySellerIdAndPeriodYm(sellerId, periodYm)
                .orElseGet(() -> SellerSettlement.create(null, sellerId, periodYm));

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

