package com.coc.modi.seller.settlement.batch;

import com.coc.modi.seller.seller.domain.Seller;
import com.coc.modi.seller.seller.infrastructure.SellerJpaRepository;
import com.coc.modi.seller.settlement.batch.support.SettlementPayoutFixture;
import com.coc.modi.seller.settlement.batch.support.SettlementPayoutTestConfig;
import com.coc.modi.seller.settlement.domain.SellerSettlement;
import com.coc.modi.seller.settlement.domain.SellerSettlementStatus;
import com.coc.modi.seller.settlement.domain.SettlementBatch;
import com.coc.modi.seller.settlement.infrastructure.SellerSettlementJpaRepository;
import com.coc.modi.seller.settlement.infrastructure.SettlementBatchJpaRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBatchTest
@SpringBootTest(
        properties = {
                "spring.config.name=application-test"
        }
)
@ActiveProfiles("test")
@Import(SettlementPayoutTestConfig.class)
class SettlementPayoutStepTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    @Qualifier("settlementPayoutTestJob")
    private Job settlementPayoutTestJob;

    @Autowired
    private SellerJpaRepository sellerJpaRepository;

    @Autowired
    private SettlementBatchJpaRepository settlementBatchJpaRepository;

    @Autowired
    private SellerSettlementJpaRepository sellerSettlementJpaRepository;

    @Autowired
    private SettlementPayoutTestConfig.RecordingSettlementPayoutWriter writer;

    @MockBean(name = "redisMessageListenerContainer")
    private RedisMessageListenerContainer redisMessageListenerContainer;

    @BeforeEach
    void setUp() {

        jobLauncherTestUtils.setJob(settlementPayoutTestJob);
        writer.reset();
        sellerSettlementJpaRepository.deleteAll();
        settlementBatchJpaRepository.deleteAll();
        sellerJpaRepository.deleteAll();
    }

    @Test
    void payoutStep_processesReadySettlements() throws Exception {

        Seller seller = sellerJpaRepository.save(SettlementPayoutFixture.newSeller(10L));
        SettlementBatch batch = settlementBatchJpaRepository.save(SettlementPayoutFixture.newBatch("2025-01"));

        SellerSettlement readyOne = SettlementPayoutFixture.newSettlement(
                batch,
                seller.getId(),
                "2025-01",
                new BigDecimal("10000"),
                new BigDecimal("1000"),
                SellerSettlementStatus.READY
        );
        SellerSettlement readyTwo = SettlementPayoutFixture.newSettlement(
                batch,
                seller.getId(),
                "2025-01",
                new BigDecimal("20000"),
                new BigDecimal("2000"),
                SellerSettlementStatus.READY
        );
        SellerSettlement paid = SettlementPayoutFixture.newSettlement(
                batch,
                seller.getId(),
                "2025-01",
                new BigDecimal("30000"),
                new BigDecimal("3000"),
                SellerSettlementStatus.PAID
        );
        sellerSettlementJpaRepository.saveAll(List.of(readyOne, readyTwo, paid));

        JobExecution execution = jobLauncherTestUtils.launchJob(jobParameters(batch.getId()));

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        List<SettlementPayoutItem> processed = writer.processedItems();
        assertThat(processed).hasSize(2);
        assertThat(processed)
                .extracting(SettlementPayoutItem::settlementId)
                .containsExactlyInAnyOrder(readyOne.getId(), readyTwo.getId());
        assertThat(processed.get(0).memberId()).isEqualTo(seller.getMemberId());
    }

    @Test
    void payoutStep_skipsDuplicatesAndZeroAmounts() throws Exception {

        Seller seller = sellerJpaRepository.save(SettlementPayoutFixture.newSeller(11L));
        SettlementBatch batch = settlementBatchJpaRepository.save(SettlementPayoutFixture.newBatch("2025-02"));

        SellerSettlement valid = SettlementPayoutFixture.newSettlement(
                batch,
                seller.getId(),
                "2025-02",
                new BigDecimal("15000"),
                new BigDecimal("1500"),
                SellerSettlementStatus.READY
        );
        SellerSettlement zeroAmount = SettlementPayoutFixture.newSettlement(
                batch,
                seller.getId(),
                "2025-02",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                SellerSettlementStatus.READY
        );
        SellerSettlement duplicate = SettlementPayoutFixture.newSettlement(
                batch,
                seller.getId(),
                "2025-02",
                new BigDecimal("12000"),
                new BigDecimal("1200"),
                SellerSettlementStatus.READY
        );
        sellerSettlementJpaRepository.saveAll(List.of(valid, zeroAmount, duplicate));

        writer.markDuplicate(duplicate.getId());

        JobExecution execution = jobLauncherTestUtils.launchJob(jobParameters(batch.getId()));

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(writer.processedItems())
                .extracting(SettlementPayoutItem::settlementId)
                .containsExactly(valid.getId());
        assertThat(writer.duplicateSettlementIds())
                .containsExactly(duplicate.getId());
        assertThat(writer.zeroAmountSettlementIds())
                .containsExactly(zeroAmount.getId());
    }

    @Test
    void payoutStep_failsOnWriterError() throws Exception {

        Seller seller = sellerJpaRepository.save(SettlementPayoutFixture.newSeller(12L));
        SettlementBatch batch = settlementBatchJpaRepository.save(SettlementPayoutFixture.newBatch("2025-03"));

        SellerSettlement first = SettlementPayoutFixture.newSettlement(
                batch,
                seller.getId(),
                "2025-03",
                new BigDecimal("11000"),
                new BigDecimal("1100"),
                SellerSettlementStatus.READY
        );
        SellerSettlement failing = SettlementPayoutFixture.newSettlement(
                batch,
                seller.getId(),
                "2025-03",
                new BigDecimal("22000"),
                new BigDecimal("2200"),
                SellerSettlementStatus.READY
        );
        sellerSettlementJpaRepository.saveAll(List.of(first, failing));

        writer.failOn(failing.getId());

        JobExecution execution = jobLauncherTestUtils.launchJob(jobParameters(batch.getId()));

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.FAILED);
        assertThat(writer.processedItems())
                .extracting(SettlementPayoutItem::settlementId)
                .isSubsetOf(Set.of(first.getId(), failing.getId()));
    }

    private JobParameters jobParameters(Long batchId) {

        return new JobParametersBuilder()
                .addLong("batchId", batchId)
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
    }
}
