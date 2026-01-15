package com.coc.modi.seller.settlement.batch.support;

import com.coc.modi.seller.settlement.batch.SettlementBatchJobListener;
import com.coc.modi.seller.settlement.batch.SettlementPayoutItem;
import com.coc.modi.seller.settlement.batch.SettlementPayoutWriter;
import com.coc.modi.seller.settlement.infrastructure.SellerSettlementJpaRepository;
import com.coc.modi.seller.settlement.infrastructure.client.wallet.WalletClientAdapter;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

@TestConfiguration
public class SettlementPayoutTestConfig {

    @Bean
    @Primary
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
    public RecordingSettlementPayoutWriter testSettlementPayoutWriter(WalletClientAdapter walletClientAdapter,
                                                                      SellerSettlementJpaRepository settlementRepository) {

        return new RecordingSettlementPayoutWriter(walletClientAdapter, settlementRepository);
    }

    @Bean
    public DataSourceInitializer batchSchemaInitializer(DataSource dataSource) {

        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("org/springframework/batch/core/schema-h2.sql"));

        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);
        initializer.setDatabasePopulator(populator);
        return initializer;
    }

    public static class RecordingSettlementPayoutWriter extends SettlementPayoutWriter {

        private final List<SettlementPayoutItem> processedItems = new ArrayList<>();
        private final List<Long> duplicateSettlementIds = new ArrayList<>();
        private final List<Long> zeroAmountSettlementIds = new ArrayList<>();
        private final Set<Long> duplicateIds = new HashSet<>();
        private final Set<Long> failOnIds = new HashSet<>();

        public void reset() {

            processedItems.clear();
            duplicateSettlementIds.clear();
            zeroAmountSettlementIds.clear();
            duplicateIds.clear();
            failOnIds.clear();
        }

        public void markDuplicate(Long settlementId) {

            if (settlementId != null) {
                duplicateIds.add(settlementId);
            }
        }

        public void failOn(Long settlementId) {

            if (settlementId != null) {
                failOnIds.add(settlementId);
            }
        }

        public List<SettlementPayoutItem> processedItems() {

            return List.copyOf(processedItems);
        }

        public List<Long> duplicateSettlementIds() {

            return List.copyOf(duplicateSettlementIds);
        }

        public List<Long> zeroAmountSettlementIds() {

            return List.copyOf(zeroAmountSettlementIds);
        }

        public RecordingSettlementPayoutWriter(WalletClientAdapter walletClientAdapter,
                                               SellerSettlementJpaRepository settlementRepository) {

            super(walletClientAdapter, settlementRepository);
        }

        @Override
        public void write(Chunk<? extends SettlementPayoutItem> chunk) {

            if (chunk == null || chunk.isEmpty()) {
                return;
            }

            for (SettlementPayoutItem item : chunk) {
                if (item == null) {
                    continue;
                }
                Long settlementId = item.settlementId();
                if (failOnIds.contains(settlementId)) {
                    throw new IllegalStateException("Forced failure for settlementId=" + settlementId);
                }
                BigDecimal amount = item.amount();
                if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
                    zeroAmountSettlementIds.add(settlementId);
                    continue;
                }
                if (duplicateIds.contains(settlementId)) {
                    duplicateSettlementIds.add(settlementId);
                    continue;
                }
                processedItems.add(item);
            }
        }
    }
}
