package com.coc.modi.account.deposit.application;

import com.coc.modi.account.deposit.application.dto.DepositApprovalCommand;
import com.coc.modi.account.deposit.application.dto.DepositCancelCommand;
import com.coc.modi.account.deposit.application.dto.DepositCommand;
import com.coc.modi.account.deposit.application.dto.DepositFailCommand;
import com.coc.modi.account.deposit.application.dto.DepositResponse;
import com.coc.modi.account.deposit.domain.PgDeposit;
import com.coc.modi.account.deposit.domain.PgDepositRepository;
import com.coc.modi.account.deposit.domain.PgDepositStatus;
import com.coc.modi.account.deposit.infrastructure.client.TossPaymentsClient;
import com.coc.modi.account.deposit.infrastructure.client.dto.TossPaymentApprovalResponse;
import com.coc.modi.account.deposit.infrastructure.client.dto.TossPaymentCancelResponse;
import com.coc.modi.account.wallet.exception.AccountException;
import com.coc.modi.account.wallet.exception.AccountTransactionNotFoundException;
import com.coc.modi.account.wallet.application.WalletCommandService;
import com.coc.modi.account.wallet.application.dto.WalletTransactionCommand;
import com.coc.modi.common.ErrorCode;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DepositService {

    private final PgDepositRepository pgDepositRepository;
    private final TossPaymentsClient tossPaymentsClient;
    private final WalletCommandService  walletCommandService;
    private static final String PG_PROVIDER = "TOSS_PAYMENTS";

    @Value("${account.deposit.card-fee-rate:0.03}")
    private BigDecimal cardFeeRate;

    private static final int MONEY_SCALE = 0;

    // 예치금 충전 요청
    @Transactional
    public DepositResponse requestDeposit(DepositCommand command) {

        String orderId = generateOrderId();

        BigDecimal amount = command.amount();
        BigDecimal feeAmount = calculateFee(amount);
        BigDecimal totalAmount = amount.add(feeAmount);

        PgDeposit pgDeposit = PgDeposit.createRequest(
                command.memberId(),
                amount,
                feeAmount,
                totalAmount,
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
        PgDeposit deposit = pgDepositRepository.findByPgTidForUpdate(command.orderId())
                .orElseThrow(() -> new AccountTransactionNotFoundException(command.orderId()));

        if (deposit.getStatus() == PgDepositStatus.SUCCESS) {

            BigDecimal requestedAmount = deposit.getTotalAmount();
            BigDecimal approvedAmount = command.amount();

            if (approvedAmount != null && requestedAmount.compareTo(approvedAmount) != 0) {

                throw new AccountException(ErrorCode.CONFLICT, "이미 승인된 결제 금액과 일치하지 않습니다.");
            }

            if (deposit.getPaymentKey() != null && !deposit.getPaymentKey().equals(command.paymentKey())) {

                throw new AccountException(ErrorCode.CONFLICT, "이미 승인된 결제와 paymentKey가 일치하지 않습니다.");
            }

            return DepositResponse.from(deposit);
        }

        if (deposit.getStatus() != PgDepositStatus.REQUESTED) {

            throw new AccountException(ErrorCode.CONFLICT, "승인할 수 없는 상태입니다. : " + deposit.getStatus());
        }

        // 2. 금액 검증
        BigDecimal requestedAmount = deposit.getTotalAmount();
        BigDecimal approvedAmount = command.amount();

        if (approvedAmount == null || requestedAmount.compareTo(approvedAmount) != 0) {

            deposit.fail("금액 불일치");

            throw new AccountException(ErrorCode.INVALID_INPUT, "요청 금액과 실제 금액이 일치하지 않습니다.");
        }

        // 3. Toss 결제 승인 API 호출
        TossPaymentApprovalResponse tossResponse = tossPaymentsClient.approvePayment(
                command.paymentKey(),
                command.orderId(),
                approvedAmount
        );

        // 4. Toss 결제 승인 결과 확인
        if (!"DONE".equals(tossResponse.status())) {

            deposit.fail("Toss 결제 승인 실패 : " + tossResponse.status());

            throw new AccountException(ErrorCode.INTERNAL_ERROR, "결제 승인에 실패했습니다.");
        }

        // 5. DB 상태 업데이트
        deposit.approve(command.paymentKey());

        // 6. 예치금 잔액 증가
        WalletTransactionCommand txCommand = WalletTransactionCommand.forDepositCharge(
                deposit.getMemberId(),
                deposit,
                deposit.getAmount(),
                deposit.getPaymentKey()
        );
        
        walletCommandService.createTransactionAndUpdateBalance(txCommand);

        return DepositResponse.from(deposit);
    }

    // 주문번호 생성
    public String generateOrderId() {

        return "ORDER_" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
    }

    // 예치금 충전 취소(환불)
    @Transactional
    public DepositResponse cancelDeposit(DepositCancelCommand command) {

        // 1. 기존 요청 조회
        PgDeposit deposit = pgDepositRepository.findByPgTid(command.orderId())
                .orElseThrow(() -> new AccountTransactionNotFoundException(command.orderId()));

        if (!deposit.getMemberId().equals(command.memberId())) {

            throw new AccountException(ErrorCode.FORBIDDEN, "본인 요청만 취소할 수 있습니다.");
        }

        // 2. 취소 가능한지 확인
        if (!deposit.isCancelable()) {

            throw new AccountException(ErrorCode.CONFLICT, "취소할 수 없는 상태입니다. : " + deposit.getStatus());
        }

        // 3. 금액 검증
        BigDecimal requestedAmount = deposit.getTotalAmount();
        BigDecimal cancelAmount = command.cancelAmount();

        if (cancelAmount == null || requestedAmount.compareTo(cancelAmount) != 0) {

            throw new AccountException(ErrorCode.INVALID_INPUT, "요청 금액과 실제 금액이 일치하지 않습니다.");
        }

        // 4. Toss 취소 API 호출
        TossPaymentCancelResponse tossResponse = tossPaymentsClient.cancelPayment(
                command.paymentKey(),
                cancelAmount,
                command.cancelReason()
        );

        if (!"CANCELED".equalsIgnoreCase(tossResponse.status())) {

            deposit.fail("Toss 결제 취소 실패 : " + tossResponse.status());

            throw new AccountException(ErrorCode.INTERNAL_ERROR, "결제 취소에 실패했습니다.");
        }

        // 5. 상태, 잔액 갱신
        deposit.cancel(command.cancelReason());

        walletCommandService.createTransactionAndUpdateBalance(
                WalletTransactionCommand.forDepositCancel(
                        deposit.getMemberId(),
                        deposit,
                        deposit.getAmount(),
                        deposit.getPaymentKey()
                )
        );

        return DepositResponse.from(deposit);
    }
	
	// 결제 실패
	@Transactional
    public DepositResponse failDeposit(DepositFailCommand command) {
		
		PgDeposit deposit = pgDepositRepository.findByPgTid(command.orderId())
				.orElseThrow(() -> new AccountTransactionNotFoundException(command.orderId()));
		
		deposit.fail(command.failureMessage());
		
        return DepositResponse.from(deposit);
    }

    private BigDecimal calculateFee(BigDecimal amount) {

        if (amount == null) {
            return BigDecimal.ZERO;
        }
        return amount.multiply(cardFeeRate).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
