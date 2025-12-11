package com.coc.modi.seller.settlement.infrastructure;

import com.coc.modi.seller.settlement.domain.SettlementBatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SettlementBatchJpaRepository extends JpaRepository<SettlementBatch, Long> {

    Optional<SettlementBatch> findByPeriodYm(String periodYm);

    Page<SettlementBatch> findByPeriodYm(String periodYm, Pageable pageable);
}
