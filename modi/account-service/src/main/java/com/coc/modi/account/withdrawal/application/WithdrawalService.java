package com.coc.modi.account.withdrawal.application;

import com.coc.modi.account.withdrawal.application.dto.WithdrawalResponse;
import com.coc.modi.account.withdrawal.domain.WithdrawalRequest;
import com.coc.modi.account.withdrawal.domain.WithdrawalRequestRepository;
import com.coc.modi.account.wallet.application.WalletCommandService;
import com.coc.modi.account.wallet.application.dto.WalletTransactionCommand;
import com.coc.modi.account.wallet.domain.WalletTransaction;
import com.coc.modi.account.wallet.exception.AccountException;
import com.coc.modi.common.ErrorCode;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class WithdrawalService {

    private static final BigDecimal WITHDRAW_FEE_RATE = new BigDecimal("0.10");
    private static final int MONEY_SCALE = 2;

    private final WithdrawalRequestRepository withdrawalRequestRepository;
    private final WalletCommandService walletCommandService;

    @Transactional
    public WithdrawalResponse requestWithdrawal(Long memberId, BigDecimal amount) {

        if (memberId == null || amount == null) {
            throw new AccountException(ErrorCode.INVALID_INPUT, "출금 요청 정보가 올바르지 않습니다.");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AccountException(ErrorCode.INVALID_INPUT, "출금 금액은 0보다 커야 합니다.");
        }

        BigDecimal feeAmount = amount.multiply(WITHDRAW_FEE_RATE).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        BigDecimal payoutAmount = amount.subtract(feeAmount).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        if (payoutAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AccountException(ErrorCode.INVALID_INPUT, "출금 금액이 너무 작습니다.");
        }

        WalletTransaction transaction = walletCommandService.createTransactionAndUpdateBalance(
                WalletTransactionCommand.forWithdrawalRequest(memberId, amount, "예치금 출금 요청")
        );

        WithdrawalRequest request = WithdrawalRequest.create(
                memberId,
                transaction.getId(),
                amount,
                feeAmount,
                payoutAmount
        );
        request.markProcessing();
        WithdrawalRequest saved = withdrawalRequestRepository.save(request);

        return WithdrawalResponse.from(saved);
    }
}
