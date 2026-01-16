package com.coc.modi.account.deposit.application;

import com.coc.modi.account.deposit.application.dto.DepositApprovalCommand;
import com.coc.modi.account.deposit.application.dto.DepositCommand;
import com.coc.modi.account.deposit.application.dto.DepositResponse;
import com.coc.modi.account.deposit.domain.PgDeposit;
import com.coc.modi.account.deposit.domain.PgDepositRepository;
import com.coc.modi.account.deposit.domain.PgDepositStatus;
import com.coc.modi.account.deposit.infrastructure.client.TossPaymentsClient;
import com.coc.modi.account.deposit.infrastructure.client.dto.TossPaymentApprovalResponse;
import com.coc.modi.account.wallet.application.WalletCommandService;
import com.coc.modi.account.wallet.domain.WalletTransactionRepository;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(
        properties = {
                "spring.config.name=application-test",
                "spring.cloud.config.enabled=false",
                "spring.cloud.config.import-check.enabled=false",
                "spring.cloud.discovery.enabled=false",
                "eureka.client.enabled=false",
                "app.gateway-prefix=test",
                "spring.datasource.url=jdbc:h2:mem:accounttest;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
                "spring.datasource.driverClassName=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.properties.hibernate.hbm2ddl.create_namespaces=true"
        }
)
class DepositServiceConcurrencyTest {

    @Autowired
    private DepositService depositService;

    @Autowired
    private WalletCommandService walletCommandService;

    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    @Autowired
    private PgDepositRepository pgDepositRepository;

    @Autowired
    private TossPaymentsClient tossPaymentsClient;

    @TestConfiguration
    static class MockConfig {

        @Bean
        @Primary
        TossPaymentsClient tossPaymentsClient() {
            return Mockito.mock(TossPaymentsClient.class);
        }
    }

    @Test
    void approveDeposit_isIdempotentWithConcurrentRequests() throws Exception {

        when(tossPaymentsClient.approvePayment(anyString(), anyString(), any()))
                .thenAnswer(invocation -> {
                    String paymentKey = invocation.getArgument(0);
                    String orderId = invocation.getArgument(1);
                    BigDecimal amount = invocation.getArgument(2);
                    return new TossPaymentApprovalResponse(
                            paymentKey,
                            orderId,
                            "DONE",
                            amount.longValue(),
                            null,
                            null,
                            null,
                            null
                    );
                });

        Long memberId = 1L;
        BigDecimal amount = new BigDecimal("1000.00");
        walletCommandService.createWalletForMember(memberId);

        DepositResponse request = depositService.requestDeposit(new DepositCommand(memberId, amount));
        String paymentKey = "payment-key";
        DepositApprovalCommand approveCommand = new DepositApprovalCommand(
                paymentKey,
                request.orderId(),
                amount
        );

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<DepositResponse>> futures = new ArrayList<>();

        try {
            for (int i = 0; i < 2; i++) {
                futures.add(executor.submit(() -> {
                    readyLatch.countDown();
                    startLatch.await();
                    return depositService.approveDeposit(memberId, approveCommand);
                }));
            }

            readyLatch.await(3, TimeUnit.SECONDS);
            startLatch.countDown();

            for (Future<DepositResponse> future : futures) {
                DepositResponse response = future.get(5, TimeUnit.SECONDS);
                assertThat(response.status()).isEqualTo(PgDepositStatus.SUCCESS);
            }
        } finally {
            executor.shutdownNow();
        }

        assertThat(walletTransactionRepository.findByMemberId(memberId)).hasSize(1);
        PgDeposit deposit = pgDepositRepository.findByPgTid(request.orderId()).orElseThrow();
        assertThat(deposit.getStatus()).isEqualTo(PgDepositStatus.SUCCESS);
        assertThat(deposit.getPaymentKey()).isEqualTo(paymentKey);
    }
}
