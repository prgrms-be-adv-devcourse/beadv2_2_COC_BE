package com.coc.modi.account.deposit.application;

import com.coc.modi.account.deposit.application.dto.DepositApprovalCommand;
import com.coc.modi.account.deposit.application.dto.DepositCancelCommand;
import com.coc.modi.account.deposit.application.dto.DepositCommand;
import com.coc.modi.account.deposit.application.dto.DepositResponse;
import com.coc.modi.account.deposit.domain.PgDeposit;
import com.coc.modi.account.deposit.domain.PgDepositRepository;
import com.coc.modi.account.deposit.domain.PgDepositStatus;
import com.coc.modi.account.deposit.infrastructure.client.TossPaymentsClient;
import com.coc.modi.account.deposit.infrastructure.client.dto.TossPaymentApprovalResponse;
import com.coc.modi.account.deposit.infrastructure.client.dto.TossPaymentCancelResponse;
import com.coc.modi.account.wallet.application.WalletCommandService;
import com.coc.modi.account.wallet.domain.MemberWalletRepository;
import com.coc.modi.account.wallet.domain.WalletTransaction;
import com.coc.modi.account.wallet.domain.WalletTransactionRepository;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.List;

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
                "spring.datasource.url=jdbc:h2:mem:accounttest;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS account",
                "spring.datasource.driverClassName=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.hibernate.ddl-auto=create-drop"
        }
)
class DepositFeePolicyTest {

    @Autowired
    private DepositService depositService;

    @Autowired
    private WalletCommandService walletCommandService;

    @Autowired
    private MemberWalletRepository memberWalletRepository;

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
    void requestDeposit_keepsRequestedAmount() {

        Long memberId = 1L;
        BigDecimal amount = new BigDecimal("1000.00");

        DepositResponse response = depositService.requestDeposit(new DepositCommand(memberId, amount));

        assertThat(response.amount()).isEqualByComparingTo("1000");
        PgDeposit deposit = pgDepositRepository.findByPgTid(response.orderId()).orElseThrow();
        assertThat(deposit.getRemainingAmount()).isEqualByComparingTo("1000");
    }

    @Test
    void approveDeposit_usesRequestedAmount() {

        String paymentKey = "payment-key-" + UUID.randomUUID();

        when(tossPaymentsClient.approvePayment(anyString(), anyString(), any()))
                .thenReturn(new TossPaymentApprovalResponse(
                        paymentKey,
                        "order-id",
                        "DONE",
                        1000L,
                        null,
                        null,
                        null,
                        null
                ));

        Long memberId = 2L;
        BigDecimal amount = new BigDecimal("1000.00");

        walletCommandService.createWalletForMember(memberId);

        DepositResponse request = depositService.requestDeposit(new DepositCommand(memberId, amount));
        DepositApprovalCommand approveCommand = new DepositApprovalCommand(
                paymentKey,
                request.orderId(),
                request.amount()
        );

        DepositResponse approved = depositService.approveDeposit(approveCommand);

        assertThat(approved.status()).isEqualTo(PgDepositStatus.SUCCESS);
        assertThat(memberWalletRepository.findByMemberId(memberId).orElseThrow().getBalance())
                .isEqualByComparingTo("1000");

        List<WalletTransaction> transactions = walletTransactionRepository.findByMemberId(memberId);
        assertThat(transactions).hasSize(1);
        assertThat(transactions.get(0).getAmount()).isEqualByComparingTo("1000");
    }

    @Test
    void cancelDeposit_refundsRequestedAmount() {

        String paymentKey = "payment-key-" + UUID.randomUUID();

        when(tossPaymentsClient.approvePayment(anyString(), anyString(), any()))
                .thenReturn(new TossPaymentApprovalResponse(
                        paymentKey,
                        "order-id",
                        "DONE",
                        1000L,
                        null,
                        null,
                        null,
                        null
                ));
        when(tossPaymentsClient.cancelPayment(anyString(), any(), anyString()))
                .thenReturn(new TossPaymentCancelResponse(
                        "CANCELED",
                        paymentKey,
                        "order-id",
                        1000L
                ));

        Long memberId = 3L;
        BigDecimal amount = new BigDecimal("1000.00");

        walletCommandService.createWalletForMember(memberId);

        DepositResponse request = depositService.requestDeposit(new DepositCommand(memberId, amount));
        DepositApprovalCommand approveCommand = new DepositApprovalCommand(
                paymentKey,
                request.orderId(),
                request.amount()
        );
        depositService.approveDeposit(approveCommand);

        DepositCancelCommand cancelCommand = new DepositCancelCommand(
                memberId,
                paymentKey,
                request.orderId(),
                request.amount(),
                "user-request"
        );
        DepositResponse canceled = depositService.cancelDeposit(cancelCommand);

        assertThat(canceled.status()).isEqualTo(PgDepositStatus.CANCELED);
        assertThat(memberWalletRepository.findByMemberId(memberId).orElseThrow().getBalance())
                .isEqualByComparingTo("0");

        List<WalletTransaction> transactions = walletTransactionRepository.findByMemberId(memberId);
        assertThat(transactions).hasSize(2);
        assertThat(transactions)
                .extracting(WalletTransaction::getAmount)
                .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .containsExactlyInAnyOrder(
                        new BigDecimal("1000"),
                        new BigDecimal("-1000")
                );
    }
}
