package com.coc.modi.account.deposit.application;

import com.coc.modi.account.deposit.application.dto.DepositApprovalCommand;
import com.coc.modi.account.deposit.application.dto.DepositCommand;
import com.coc.modi.account.deposit.application.dto.DepositResponse;
import com.coc.modi.account.deposit.domain.PgDeposit;
import com.coc.modi.account.deposit.domain.PgDepositRepository;
import com.coc.modi.account.deposit.infrastructure.client.TossPaymentsClient;
import com.coc.modi.account.deposit.infrastructure.client.dto.TossPaymentApprovalResponse;
import com.coc.modi.account.wallet.application.WalletCommandService;
import com.coc.modi.account.wallet.application.dto.WalletTransactionCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DepositService {

    private final PgDepositRepository pgDepositRepository;
    private final TossPaymentsClient tossPaymentsClient;
    private final WalletCommandService  walletCommandService;
    private static final String PG_PROVIDER = "TOSS_PAYMENTS";

    // 예치금 충전 요청
    public DepositResponse requestDeposit(DepositCommand command) {

        String orderId = generateOrderId();

        PgDeposit pgDeposit = PgDeposit.createRequest(
                command.memberId(),
                command.amount(),
                PG_PROVIDER,
                orderId
        );

        PgDeposit save = pgDepositRepository.save(pgDeposit);

        return DepositResponse.from(save);
    }

    // 예치금 충전 승인
    @Transactional
    public DepositResponse approveDeposit(DepositApprovalCommand command) {

        // 1. orderId로 충전 요청 조회
        PgDeposit deposit = pgDepositRepository.findByPgTid(command.orderId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 요청입니다."));

        // 2. 금액 검증
        BigDecimal requestedAmount = deposit.getAmount();
        BigDecimal approvedAmount = command.amount();
        if(approvedAmount == null || requestedAmount.compareTo(approvedAmount) != 0){

            deposit.fail("금액 불일치");

            throw new IllegalArgumentException("요청 금액과 실제 금액이 일치하지 않습니다.");
        }

        // 3. Toss 결제 승인 API 호출
        TossPaymentApprovalResponse tossResponse = tossPaymentsClient.approvePayment(
                command.paymentKey(),
                command.orderId(),
                approvedAmount
        );

        // 4. Toss 결제 승인 결과 확인
        if(!"DONE".equals(tossResponse.status())){

            deposit.fail("Toss 결제 승인 실패 : " + tossResponse.status());

            throw new IllegalStateException("결제 승인에 실패했습니다.");
        }

        // 5. DB 상태 업데이트
        deposit.approve();

        // 6. 예치금 잔액 증가
        WalletTransactionCommand txCommand = WalletTransactionCommand.forDepositCharge(deposit.getMemberId(), deposit.getId(), deposit.getAmount());
        
        walletCommandService.createTransactionAndUpdateBalance(txCommand);

        return DepositResponse.from(deposit);
    }

    // 주문번호 생성
    public String generateOrderId() {
        return "ORDER_" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
    }
}
