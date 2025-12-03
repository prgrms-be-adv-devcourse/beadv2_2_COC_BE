package com.coc.modi.seller.settlement.infrastructure;

import com.coc.modi.seller.settlement.domain.SellerSettlement;
import com.coc.modi.seller.settlement.domain.SellerSettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SellerSettlementRepositoryAdapter implements SellerSettlementRepository {

    private final SellerSettlementJpaRepository sellerSettlementJpaRepository;

    @Override
    public Page<SellerSettlement> findBySellerId(Long sellerId, Pageable pageable) {
        return sellerSettlementJpaRepository.findBySellerId(sellerId, pageable);
    }

    @Override
    public Optional<SellerSettlement> findById(Long sellerSettlementId) {
        return sellerSettlementJpaRepository.findById(sellerSettlementId);
    }

    @Override
    public SellerSettlement save(SellerSettlement settlement) {
        return sellerSettlementJpaRepository.save(settlement);
    }
}
