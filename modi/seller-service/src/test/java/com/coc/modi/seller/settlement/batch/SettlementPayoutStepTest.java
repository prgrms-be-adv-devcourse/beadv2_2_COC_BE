package com.coc.modi.seller.settlement.batch;

import com.coc.modi.seller.seller.domain.Seller;
import com.coc.modi.seller.seller.infrastructure.SellerJpaRepository;
import com.coc.modi.seller.settlement.batch.support.SettlementPayoutFixture;
import com.coc.modi.seller.settlement.batch.support.SettlementPayoutTestConfig;
import com.coc.modi.seller.settlement.application.SettlementNotificationService;
import com.coc.modi.seller.settlement.application.SettlementPayoutRequestPublisher;
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
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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

    @MockBean
    private SettlementPayoutRequestPublisher settlementPayoutRequestPublisher;

    @MockBean
    private SettlementNotificationService settlementNotificationService;

    @MockBean
    private RedisMessageListenerContainer redisMessageListenerContainer;

    @MockBean
    private RedisConnectionFactory redisConnectionFactory;

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    @MockBean
    private ReactiveRedisConnectionFactory reactiveRedisConnectionFactory;

    @MockBean(name = "redisMessageListenerContainer")
    private RedisMessageListenerContainer redisMessageListenerContainer;

    @BeforeEach
    void setUp() {

        jobLauncherTestUtils.setJob(settlementPayoutTestJob);
        reset(settlementPayoutRequestPublisher, settlementNotificationService);
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

        SellerSettlement updatedReadyOne = sellerSettlementJpaRepository.findById(readyOne.getId()).orElseThrow();
        SellerSettlement updatedReadyTwo = sellerSettlementJpaRepository.findById(readyTwo.getId()).orElseThrow();
        SellerSettlement updatedPaid = sellerSettlementJpaRepository.findById(paid.getId()).orElseThrow();

        assertThat(updatedReadyOne.getStatus()).isEqualTo(SellerSettlementStatus.PENDING);
        assertThat(updatedReadyTwo.getStatus()).isEqualTo(SellerSettlementStatus.PENDING);
        assertThat(updatedPaid.getStatus()).isEqualTo(SellerSettlementStatus.PAID);

        verify(settlementPayoutRequestPublisher, times(2))
                .publish(anyLong(), anyLong(), anyLong(), any(BigDecimal.class));
        verify(settlementPayoutRequestPublisher).publish(eq(readyOne.getId()), anyLong(), anyLong(), any(BigDecimal.class));
        verify(settlementPayoutRequestPublisher).publish(eq(readyTwo.getId()), anyLong(), anyLong(), any(BigDecimal.class));
        verify(settlementNotificationService, times(0)).notifySettlementPaid(any());
    }

    @Test
    void payoutStep_marksZeroAmountAsPaid() throws Exception {

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

        JobExecution execution = jobLauncherTestUtils.launchJob(jobParameters(batch.getId()));

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        SellerSettlement updatedValid = sellerSettlementJpaRepository.findById(valid.getId()).orElseThrow();
        SellerSettlement updatedZero = sellerSettlementJpaRepository.findById(zeroAmount.getId()).orElseThrow();
        SellerSettlement updatedDuplicate = sellerSettlementJpaRepository.findById(duplicate.getId()).orElseThrow();

        assertThat(updatedValid.getStatus()).isEqualTo(SellerSettlementStatus.PENDING);
        assertThat(updatedZero.getStatus()).isEqualTo(SellerSettlementStatus.PAID);
        assertThat(updatedDuplicate.getStatus()).isEqualTo(SellerSettlementStatus.PENDING);

        verify(settlementPayoutRequestPublisher, times(2))
                .publish(anyLong(), anyLong(), anyLong(), any(BigDecimal.class));
        verify(settlementPayoutRequestPublisher).publish(eq(valid.getId()), anyLong(), anyLong(), any(BigDecimal.class));
        verify(settlementPayoutRequestPublisher).publish(eq(duplicate.getId()), anyLong(), anyLong(), any(BigDecimal.class));
        verify(settlementNotificationService, times(1)).notifySettlementPaid(any());
    }

    @Test
    void payoutStep_skipsNonReadySettlements() throws Exception {

        Seller seller = sellerJpaRepository.save(SettlementPayoutFixture.newSeller(12L));
        SettlementBatch batch = settlementBatchJpaRepository.save(SettlementPayoutFixture.newBatch("2025-03"));

        SellerSettlement ready = SettlementPayoutFixture.newSettlement(
                batch,
                seller.getId(),
                "2025-03",
                new BigDecimal("11000"),
                new BigDecimal("1100"),
                SellerSettlementStatus.READY
        );
        SellerSettlement paid = SettlementPayoutFixture.newSettlement(
                batch,
                seller.getId(),
                "2025-03",
                new BigDecimal("22000"),
                new BigDecimal("2200"),
                SellerSettlementStatus.PAID
        );
        SellerSettlement pending = SettlementPayoutFixture.newSettlement(
                batch,
                seller.getId(),
                "2025-03",
                new BigDecimal("33000"),
                new BigDecimal("3300"),
                SellerSettlementStatus.READY
        );
        pending.requestPayout();
        sellerSettlementJpaRepository.saveAll(List.of(ready, paid, pending));

        JobExecution execution = jobLauncherTestUtils.launchJob(jobParameters(batch.getId()));

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        SellerSettlement updatedReady = sellerSettlementJpaRepository.findById(ready.getId()).orElseThrow();
        SellerSettlement updatedPaid = sellerSettlementJpaRepository.findById(paid.getId()).orElseThrow();
        SellerSettlement updatedPending = sellerSettlementJpaRepository.findById(pending.getId()).orElseThrow();

        assertThat(updatedReady.getStatus()).isEqualTo(SellerSettlementStatus.PENDING);
        assertThat(updatedPaid.getStatus()).isEqualTo(SellerSettlementStatus.PAID);
        assertThat(updatedPending.getStatus()).isEqualTo(SellerSettlementStatus.PENDING);

        verify(settlementPayoutRequestPublisher, times(1))
                .publish(eq(ready.getId()), anyLong(), anyLong(), any(BigDecimal.class));
    }

    private JobParameters jobParameters(Long batchId) {

        return new JobParametersBuilder()
                .addLong("batchId", batchId)
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
    }
}
