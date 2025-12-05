package com.coc.modi.seller.settlement.domain;

import com.coc.modi.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "settlement_batch")
public class SettlementBatch extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "period_ym", nullable = false, length = 7)
    private String periodYm;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SettlementBatchStatus status;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Builder
    private SettlementBatch(String periodYm,
                            SettlementBatchStatus status,
                            LocalDateTime startedAt,
                            LocalDateTime completedAt) {
        this.periodYm = periodYm;
        this.status = status != null ? status : SettlementBatchStatus.READY;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
    }

    public static SettlementBatch create(String periodYm) {
        return SettlementBatch.builder()
                .periodYm(periodYm)
                .status(SettlementBatchStatus.READY)
                .build();
    }

    public void start(LocalDateTime startedAt) {
        this.status = SettlementBatchStatus.CALCULATING;
        this.startedAt = startedAt;
    }

    public void complete(LocalDateTime completedAt) {
        this.status = SettlementBatchStatus.COMPLETED;
        this.completedAt = completedAt;
    }

    public void fail() {
        this.status = SettlementBatchStatus.FAILED;
    }
}
