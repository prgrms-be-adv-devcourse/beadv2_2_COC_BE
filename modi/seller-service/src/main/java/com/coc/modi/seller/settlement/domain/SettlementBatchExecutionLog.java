package com.coc.modi.seller.settlement.domain;

import com.coc.modi.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "settlement_batch_execution_log")
public class SettlementBatchExecutionLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "execution_id")
    private SettlementBatchExecution execution;

    @Column(name = "step_name", length = 100)
    private String stepName;

    @Column(name = "cursor", length = 100)
    private String cursor;

    @Column(name = "processed_count")
    private Integer processedCount;

    @Column(name = "success_count")
    private Integer successCount;

    @Column(name = "fail_count")
    private Integer failCount;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Builder
    private SettlementBatchExecutionLog(SettlementBatchExecution execution,
                                        String stepName,
                                        String cursor,
                                        Integer processedCount,
                                        Integer successCount,
                                        Integer failCount,
                                        Long durationMs,
                                        String errorMessage) {
        this.execution = execution;
        this.stepName = stepName;
        this.cursor = cursor;
        this.processedCount = processedCount;
        this.successCount = successCount;
        this.failCount = failCount;
        this.durationMs = durationMs;
        this.errorMessage = errorMessage;
    }
}
