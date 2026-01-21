package com.coc.modi.account.wallet.application;

import com.coc.modi.account.wallet.exception.AccountAlreadyExistsException;
import com.coc.modi.account.wallet.exception.AccountNotFoundException;
import com.coc.modi.account.wallet.exception.InsufficientBalanceException;
import com.coc.modi.account.wallet.application.dto.RentalPaymentCommand;
import com.coc.modi.account.wallet.application.dto.RentalPaymentResponse;
import com.coc.modi.account.wallet.application.dto.RentalRefundCommand;
import com.coc.modi.account.wallet.application.dto.WalletTransactionCommand;
import com.coc.modi.account.wallet.domain.MemberWallet;
import com.coc.modi.account.wallet.domain.MemberWalletRepository;
import com.coc.modi.account.wallet.domain.WalletTransaction;
import com.coc.modi.account.wallet.domain.WalletTransactionRepository;
import com.coc.modi.account.wallet.domain.WalletTransactionType;
import com.coc.modi.account.wallet.exception.AccountException;
import com.coc.modi.common.ErrorCode;

import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class WalletCommandService {

    private final MemberWalletRepository memberWalletRepository;
    private final WalletTransactionRepository walletTransactionRepository;


    // 회원 지갑 생성
    @Transactional
    public void createWalletForMember(Long memberId) {

        memberWalletRepository.findByMemberId(memberId)
                .ifPresent(memberWallet -> {

                    throw new AccountAlreadyExistsException(String.valueOf(memberId));
                });

        MemberWallet wallet = MemberWallet.create(memberId);

        memberWalletRepository.save(wallet);
    }



    // 예치금 잔액 변경 및 트랜잭션 생성
    @Transactional
    public void createTransactionAndUpdateBalance(WalletTransactionCommand command) {

        // 1. 예치금 조회
        MemberWallet wallet = memberWalletRepository.findByMemberId(command.memberId())
                .orElseThrow(() -> new AccountNotFoundException(command.memberId()));

        // 2. txType에 따라 예치금 입금, 차감 결정
        BigDecimal signedAmount = switch (command.txType()) {

            case DEPOSIT_CHARGE, RENTAL_REFUND, ADJUST, SETTLEMENT_PAYOUT -> command.amount();
            case DEPOSIT_CANCEL, RENTAL_PAYMENT -> command.amount().negate();
        };

        BigDecimal balanceAfter = wallet.getBalance().add(signedAmount);

        // 2-1. 잔액 부족 체크
        if (balanceAfter.compareTo(BigDecimal.ZERO) < 0) {

            throw new InsufficientBalanceException("잔액 부족");
        }

        // 3. WalletTransaction 생성
        WalletTransaction tx = WalletTransaction.create(
                wallet,
                command.txType(),
                signedAmount,
                balanceAfter,
                command.pgDeposit(),
                command.relatedRentalId(),
                command.relatedRentalItemId(),
                command.relatedSettlementId(),
                command.description(),
				command.paymentKey(),
				command.requestId()
        );

        // 4. 예치금 잔액 변경
        wallet.changeBalance(balanceAfter);

        // 5. 저장
        memberWalletRepository.save(wallet);
        walletTransactionRepository.save(tx);

    }

    // Rental 결제 전용
    @Transactional
    public RentalPaymentResponse payForRental(RentalPaymentCommand command) {

        Long memberId = command.memberId();
        Long rentalId = command.rentalId();
        BigDecimal amount = command.amount();

		if (command.requestId() != null) {
			WalletTransaction existing = findByRequestId(WalletTransactionType.RENTAL_PAYMENT, command.requestId());
			if (existing != null) {
				MemberWallet wallet = memberWalletRepository.findByMemberId(memberId)
						.orElseThrow(() -> new AccountNotFoundException(memberId));
				return RentalPaymentResponse.from(wallet);
			}
		}

        WalletTransactionCommand txCommand = WalletTransactionCommand.forRentalPayment(
				memberId, rentalId, amount, command.requestId());

        try {
            createTransactionAndUpdateBalance(txCommand);
        } catch (DataIntegrityViolationException ex) {
            WalletTransaction existing = findByRequestId(WalletTransactionType.RENTAL_PAYMENT, command.requestId());
            if (existing == null) {
                throw ex;
            }
        }

        // 차감 후 지갑 상태 조회
        MemberWallet wallet = memberWalletRepository.findByMemberId(memberId)
                .orElseThrow(() -> new AccountNotFoundException(memberId));

        return RentalPaymentResponse.from(wallet);
    }

    @Transactional
    public RentalPaymentResponse refundForRental(RentalRefundCommand command) {

        Long memberId = command.memberId();
		
		if (command.requestId() != null) {
			WalletTransaction existing = findByRequestId(WalletTransactionType.RENTAL_REFUND, command.requestId());
			if (existing != null) {
				MemberWallet wallet = memberWalletRepository.findByMemberId(memberId)
						.orElseThrow(() -> new AccountNotFoundException(memberId));
				return RentalPaymentResponse.from(wallet);
			}
		}

        WalletTransactionCommand txCommand = WalletTransactionCommand.forRentalRefund(
                memberId,
                command.rentalId(),
                command.rentalItemId(),
                command.amount(),
                String.format("렌탈 환불 (itemId=%d)", command.rentalItemId()),
				command.requestId()
        );

        try {
            createTransactionAndUpdateBalance(txCommand);
        } catch (DataIntegrityViolationException ex) {
            WalletTransaction existing = findByRequestId(WalletTransactionType.RENTAL_REFUND, command.requestId());
            if (existing == null) {
                throw ex;
            }
        }

        MemberWallet wallet = memberWalletRepository.findByMemberId(memberId)
                .orElseThrow(() -> new AccountNotFoundException(memberId));

        return RentalPaymentResponse.from(wallet);
    }

	@Transactional
	public boolean payoutSettlement(Long memberId, Long settlementId, BigDecimal amount) {

		if (memberId == null || settlementId == null || amount == null) {
			throw new AccountException(ErrorCode.INVALID_INPUT, "정산 지급 요청 정보가 올바르지 않습니다.");
		}
		if (amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new AccountException(ErrorCode.INVALID_INPUT, "정산 지급 금액은 0보다 커야 합니다.");
		}

		boolean alreadyProcessed = walletTransactionRepository.existsByRelatedSettlementIdAndTxType(
				settlementId,
				WalletTransactionType.SETTLEMENT_PAYOUT
		);
		if (alreadyProcessed) {
			return false;
		}

		try {
			createTransactionAndUpdateBalance(
					WalletTransactionCommand.forSettlementPayout(memberId, settlementId, amount)
			);
			return true;
		} catch (DataIntegrityViolationException ex) {
			return false;
		}
	}

	private WalletTransaction findByRequestId(WalletTransactionType type, String requestId) {

		if (requestId == null || requestId.isBlank()) {
			return null;
		}

		return walletTransactionRepository.findByTxTypeAndRequestId(type, requestId).orElse(null);
	}
}
