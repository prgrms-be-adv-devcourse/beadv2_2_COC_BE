package com.coc.modi.seller.settlement.infrastructure;

import com.coc.modi.seller.settlement.domain.SettlementBatch;
import com.coc.modi.seller.settlement.domain.SettlementBatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SettlementBatchRepositoryAdapter implements SettlementBatchRepository {

    private final SettlementBatchJpaRepository settlementBatchJpaRepository;

    @Override
    public Optional<SettlementBatch> findByPeriodYm(String periodYm) {
        return settlementBatchJpaRepository.findByPeriodYm(periodYm);
    }

    @Override
    public Page<SettlementBatch> findByPeriodYm(String periodYm, Pageable pageable) {
        return settlementBatchJpaRepository.findByPeriodYm(periodYm, pageable);
    }

    @Override
    public Page<SettlementBatch> findAll(Pageable pageable) {
        return settlementBatchJpaRepository.findAll(pageable);
    }

    @Override
    public Optional<SettlementBatch> findById(Long batchId) {
        return settlementBatchJpaRepository.findById(batchId);
    }

    @Override
    public SettlementBatch save(SettlementBatch batch) {
        return settlementBatchJpaRepository.save(batch);
    }
}
