package com.coc.modi.seller.settlement.batch.support;

import com.coc.modi.seller.settlement.application.SettlementNotificationService;
import com.coc.modi.seller.settlement.application.SettlementPayoutRequestPublisher;
import com.coc.modi.seller.settlement.batch.SettlementBatchJobListener;
import com.coc.modi.seller.settlement.batch.SettlementPayoutItem;
import com.coc.modi.seller.settlement.batch.SettlementPayoutWriter;
import com.coc.modi.seller.settlement.infrastructure.SellerSettlementJpaRepository;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

@TestConfiguration
public class SettlementPayoutTestConfig {

    @Bean
    public Job settlementPayoutTestJob(JobRepository jobRepository,
                                       Step settlementPayoutStep,
                                       SettlementBatchJobListener settlementBatchJobListener) {

        return new JobBuilder("settlementPayoutTestJob", jobRepository)
                .listener(settlementBatchJobListener)
                .start(settlementPayoutStep)
                .build();
    }

    @Bean
    @Primary
    public RecordingSettlementPayoutWriter recordingSettlementPayoutWriter(
            SellerSettlementJpaRepository settlementRepository,
            SettlementPayoutRequestPublisher settlementPayoutRequestPublisher,
            SettlementNotificationService settlementNotificationService
    ) {

        return new RecordingSettlementPayoutWriter(
                settlementRepository,
                settlementPayoutRequestPublisher,
                settlementNotificationService
        );
    }

    public static class RecordingSettlementPayoutWriter extends SettlementPayoutWriter {

        private final List<Long> settlementIds = new ArrayList<>();

        public RecordingSettlementPayoutWriter(SellerSettlementJpaRepository settlementRepository,
                                               SettlementPayoutRequestPublisher settlementPayoutRequestPublisher,
                                               SettlementNotificationService settlementNotificationService) {

            super(settlementRepository, settlementPayoutRequestPublisher, settlementNotificationService);
        }

        @Override
        public void write(Chunk<? extends SettlementPayoutItem> chunk) {

            if (chunk != null) {
                for (SettlementPayoutItem item : chunk) {
                    if (item != null && item.settlementId() != null) {
                        settlementIds.add(item.settlementId());
                    }
                }
            }
            super.write(chunk);
        }

        public void reset() {

            settlementIds.clear();
        }

        public List<Long> recordedSettlementIds() {

            return List.copyOf(settlementIds);
        }
    }

    @Bean
    public DataSourceInitializer batchSchemaInitializer(DataSource dataSource) {

        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("org/springframework/batch/core/schema-h2.sql"));
        populator.setContinueOnError(true);

        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);
        initializer.setDatabasePopulator(populator);
        return initializer;
    }
}
