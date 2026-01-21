package com.coc.modi.seller.settlement.batch;

import com.coc.modi.seller.seller.domain.Seller;
import com.coc.modi.seller.seller.infrastructure.SellerJpaRepository;
import com.coc.modi.seller.settlement.batch.support.SettlementPayoutFixture;
import com.coc.modi.seller.settlement.batch.support.SettlementPayoutTestConfig;
import com.coc.modi.seller.settlement.domain.SellerSettlement;
import com.coc.modi.seller.settlement.domain.SellerSettlementStatus;
import com.coc.modi.seller.settlement.domain.SettlementBatch;
import com.coc.modi.seller.settlement.domain.SettlementBatchExecutionLog;
import com.coc.modi.seller.settlement.domain.SettlementBatchExecutionLogEventType;
import com.coc.modi.seller.settlement.domain.SettlementBatchExecutionLogRepository;
import com.coc.modi.seller.settlement.domain.SettlementBatchExecutionRepository;
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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBatchTest
@SpringBootTest(
        properties = {
                "spring.config.name=application-test"
        }
)
@ActiveProfiles("test")
@Import(SettlementPayoutTestConfig.class)
class SettlementBatchExecutionLogTest {

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
    private SettlementBatchExecutionRepository settlementBatchExecutionRepository;

    @Autowired
    private SettlementBatchExecutionLogRepository settlementBatchExecutionLogRepository;

    @Autowired
    private SettlementPayoutTestConfig.RecordingSettlementPayoutWriter writer;

    @MockBean(name = "redisMessageListenerContainer")
    private RedisMessageListenerContainer redisMessageListenerContainer;

    @BeforeEach
    void setUp() {

        jobLauncherTestUtils.setJob(settlementPayoutTestJob);
        writer.reset();
        settlementBatchExecutionLogRepository.deleteAll();
        settlementBatchExecutionRepository.deleteAll();
        sellerSettlementJpaRepository.deleteAll();
        settlementBatchJpaRepository.deleteAll();
        sellerJpaRepository.deleteAll();
    }

    @Test
    void recordsExecutionLogsForBatchRun() throws Exception {

        Seller seller = sellerJpaRepository.save(SettlementPayoutFixture.newSeller(20L));
        SettlementBatch batch = settlementBatchJpaRepository.save(SettlementPayoutFixture.newBatch("2025-04"));
        SellerSettlement ready = SettlementPayoutFixture.newSettlement(
                batch,
                seller.getId(),
                "2025-04",
                new BigDecimal("10000"),
                new BigDecimal("1000"),
                SellerSettlementStatus.READY
        );
        sellerSettlementJpaRepository.save(ready);

        JobExecution execution = jobLauncherTestUtils.launchJob(jobParameters(batch.getId()));

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        List<SettlementBatchExecutionLog> logs = settlementBatchExecutionLogRepository.findAll();
        assertThat(logs).isNotEmpty();
        assertThat(logs)
                .extracting(SettlementBatchExecutionLog::getEventType)
                .contains(
                        SettlementBatchExecutionLogEventType.STARTED,
                        SettlementBatchExecutionLogEventType.COMPLETED
                );
    }

    private JobParameters jobParameters(Long batchId) {

        return new JobParametersBuilder()
                .addLong("batchId", batchId)
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
    }
}
