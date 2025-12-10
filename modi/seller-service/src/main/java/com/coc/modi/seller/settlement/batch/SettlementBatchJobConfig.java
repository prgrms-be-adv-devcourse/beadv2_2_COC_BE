package com.coc.modi.seller.settlement.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class SettlementBatchJobConfig {

    private final SettlementBatchJobListener jobListener;
    private final SettlementBatchStepListener stepListener;

    @Bean
    public Job settlementAggregationJob(JobRepository jobRepository,
                                        Step settlementAggregationStep) {
        return new JobBuilder("settlementAggregationJob", jobRepository)
                .listener(jobListener)
                .start(settlementAggregationStep)
                .build();
    }

    @Bean
    public Step settlementAggregationStep(JobRepository jobRepository,
                                          PlatformTransactionManager transactionManager) {
        return new StepBuilder("settlementAggregationStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    // 외부 렌탈 API 스펙 확정 후 Reader/Processor/Writer로 교체 예정
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .listener(stepListener)
                .build();
    }
}
