package com.coc.modi.seller.settlement.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface SettlementBatchRepository {

    Optional<SettlementBatch> findByPeriodYm(String periodYm);

    Page<SettlementBatch> findByPeriodYm(String periodYm, Pageable pageable);

    Page<SettlementBatch> findAll(Pageable pageable);

    Optional<SettlementBatch> findById(Long batchId);

    SettlementBatch save(SettlementBatch batch);
}
