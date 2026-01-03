package com.coc.modi.seller.settlement.infrastructure;

import com.coc.modi.seller.settlement.domain.SellerSettlement;
import com.coc.modi.seller.settlement.domain.SellerSettlementStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SellerSettlementJpaRepository extends JpaRepository<SellerSettlement, Long> {

    Page<SellerSettlement> findBySellerId(Long sellerId, Pageable pageable);

    Page<SellerSettlement> findBySellerIdAndPeriodYm(Long sellerId, String periodYm, Pageable pageable);

    Optional<SellerSettlement> findBySellerIdAndPeriodYm(Long sellerId, String periodYm);

    List<SellerSettlement> findByBatch_IdAndStatusOrderByIdAsc(Long batchId, SellerSettlementStatus status);
}
