package com.coc.modi.seller.settlement.application;

import com.coc.modi.seller.settlement.application.dto.SettlementBatchCreateCommand;
import com.coc.modi.seller.settlement.application.dto.SettlementBatchResponse;
import com.coc.modi.seller.settlement.domain.SettlementBatch;
import com.coc.modi.seller.settlement.domain.SettlementBatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementBatchService {

    private final SettlementBatchRepository settlementBatchRepository;

    @Transactional
    public SettlementBatchResponse createBatch(SettlementBatchCreateCommand command) {
        settlementBatchRepository.findByPeriodYm(command.periodYm())
                .ifPresent(batch -> {
                    throw new IllegalArgumentException("이미 생성된 정산 배치입니다. periodYm=" + command.periodYm());
                });

        SettlementBatch batch = SettlementBatch.create(command.periodYm());
        SettlementBatch saved = settlementBatchRepository.save(batch);
        return SettlementBatchResponse.from(saved);
    }

    @Transactional
    public SettlementBatchResponse startBatch(Long batchId) {
        SettlementBatch batch = findBatch(batchId);
        batch.start(LocalDateTime.now());
        return SettlementBatchResponse.from(batch);
    }

    @Transactional
    public SettlementBatchResponse completeBatch(Long batchId) {
        SettlementBatch batch = findBatch(batchId);
        batch.complete(LocalDateTime.now());
        return SettlementBatchResponse.from(batch);
    }

    public SettlementBatchResponse getBatch(Long batchId) {
        SettlementBatch batch = findBatch(batchId);
        return SettlementBatchResponse.from(batch);
    }

    public Page<SettlementBatchResponse> getBatches(String periodYm, Pageable pageable) {
        if (periodYm != null && !periodYm.isBlank()) {
            return settlementBatchRepository.findByPeriodYm(periodYm, pageable)
                    .map(SettlementBatchResponse::from);
        }
        return settlementBatchRepository.findAll(pageable)
                .map(SettlementBatchResponse::from);
    }

    private SettlementBatch findBatch(Long batchId) {
        return settlementBatchRepository.findById(batchId)
                .orElseThrow(() -> new IllegalArgumentException("정산 배치를 찾을 수 없습니다. id=" + batchId));
    }
}
