package com.coc.modi.seller.settlement.application;

import com.coc.modi.seller.application.port.RentalPort;
import com.coc.modi.seller.settlement.domain.SellerSettlement;
import com.coc.modi.seller.settlement.domain.SellerSettlementLine;
import com.coc.modi.seller.settlement.domain.SellerSettlementRepository;
import com.coc.modi.seller.infrastructure.client.rental.dto.RentalItemInfo;
import com.coc.modi.seller.infrastructure.client.rental.dto.RentalListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SettlementAggregationService {

    private static final BigDecimal FEE_RATE = new BigDecimal("0.10");

    private final SellerSettlementRepository sellerSettlementRepository;
    private final RentalPort rentalPort;

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

    public void aggregateFromRental(Long sellerId,
                                    String periodYm,
                                    String status,
                                    String startDate,
                                    String endDate,
                                    Integer page,
                                    Integer size) {
        RentalListResponse response = rentalPort.getRentals(
                sellerId,
                status,
                startDate,
                endDate,
                null,
                page != null ? page : 0,
                size != null ? size : 100
        );
        List<RentalItemInfo> rentals = response.content();
        if (rentals == null || rentals.isEmpty()) {
            return;
        }
        rentals.forEach(rental -> aggregateLine(
                rental.sellerId(),
                resolvePeriodYm(periodYm, rental.paidAt(), rental.startDate(), rental.endDate()),
                rental.rentalItemId(),
                rental.memberId(),
                rental.productId(),
                rental.totalAmount()
        ));
    }

    private String resolvePeriodYm(String requestedPeriodYm,
                                   java.time.LocalDateTime paidAt,
                                   java.time.LocalDate startDate,
                                   java.time.LocalDate endDate) {
        if (requestedPeriodYm != null && !requestedPeriodYm.isBlank()) {
            return requestedPeriodYm;
        }
        if (paidAt != null) {
            return YearMonth.from(paidAt.toLocalDate()).toString();
        }
        LocalDate fallback = startDate != null ? startDate : endDate;
        if (fallback != null) {
            return YearMonth.from(fallback).toString();
        }
        return null;
    }
}

