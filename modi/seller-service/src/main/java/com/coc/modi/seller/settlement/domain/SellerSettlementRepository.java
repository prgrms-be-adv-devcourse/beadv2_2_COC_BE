package com.coc.modi.seller.settlement.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface SellerSettlementRepository {

    Page<SellerSettlement> findBySellerId(Long sellerId, Pageable pageable);

    Page<SellerSettlement> findBySellerIdAndPeriodYm(Long sellerId, String periodYm, Pageable pageable);

    Optional<SellerSettlement> findBySellerIdAndPeriodYm(Long sellerId, String periodYm);

    Optional<SellerSettlement> findById(Long sellerSettlementId);

    SellerSettlement save(SellerSettlement settlement);
}
