package com.coc.modi.account.wallet.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import com.coc.modi.account.wallet.domain.MemberWallet;
import com.coc.modi.account.wallet.domain.MemberWalletRepository;
import com.coc.modi.account.wallet.domain.WalletTransaction;
import com.coc.modi.account.wallet.domain.WalletTransactionRepository;
import com.coc.modi.account.wallet.domain.WalletTransactionType;
import com.coc.modi.account.wallet.exception.AccountException;
import com.coc.modi.common.ErrorCode;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class WalletCommandServiceTest {

	@Mock
	private MemberWalletRepository memberWalletRepository;

	@Mock
	private WalletTransactionRepository walletTransactionRepository;

	@InjectMocks
	private WalletCommandService walletCommandService;

	@Test
	void payoutSettlement_createsTransactionAndUpdatesBalance() {
		Long memberId = 1L;
		Long settlementId = 10L;
		BigDecimal amount = new BigDecimal("1000.00");
		MemberWallet wallet = MemberWallet.create(memberId);

		when(walletTransactionRepository.existsByRelatedSettlementIdAndTxType(
				settlementId, WalletTransactionType.SETTLEMENT_PAYOUT
		)).thenReturn(false);
		when(memberWalletRepository.findByMemberId(memberId)).thenReturn(Optional.of(wallet));
		when(walletTransactionRepository.save(any(WalletTransaction.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));
		when(memberWalletRepository.save(any(MemberWallet.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		boolean processed = walletCommandService.payoutSettlement(memberId, settlementId, amount);

		assertThat(processed).isTrue();
		assertThat(wallet.getBalance()).isEqualByComparingTo(amount);
		ArgumentCaptor<WalletTransaction> txCaptor = ArgumentCaptor.forClass(WalletTransaction.class);
		verify(walletTransactionRepository).save(txCaptor.capture());
		WalletTransaction tx = txCaptor.getValue();
		assertThat(tx.getTxType()).isEqualTo(WalletTransactionType.SETTLEMENT_PAYOUT);
		assertThat(tx.getAmount()).isEqualByComparingTo(amount);
		assertThat(tx.getRelatedSettlementId()).isEqualTo(settlementId);
	}

	@Test
	void payoutSettlement_returnsFalseWhenAlreadyProcessed() {
		when(walletTransactionRepository.existsByRelatedSettlementIdAndTxType(
				10L, WalletTransactionType.SETTLEMENT_PAYOUT
		)).thenReturn(true);

		boolean processed = walletCommandService.payoutSettlement(1L, 10L, new BigDecimal("1000.00"));

		assertThat(processed).isFalse();
		verify(memberWalletRepository, never()).findByMemberId(any());
		verify(walletTransactionRepository, never()).save(any());
	}

	@Test
	void payoutSettlement_throwsOnInvalidInput() {
		assertThatThrownBy(() -> walletCommandService.payoutSettlement(null, 10L, new BigDecimal("1000.00")))
				.isInstanceOf(AccountException.class)
				.satisfies(ex -> assertThat(((AccountException) ex).getErrorCode())
						.isEqualTo(ErrorCode.INVALID_INPUT));

		assertThatThrownBy(() -> walletCommandService.payoutSettlement(1L, 10L, BigDecimal.ZERO))
				.isInstanceOf(AccountException.class)
				.satisfies(ex -> assertThat(((AccountException) ex).getErrorCode())
						.isEqualTo(ErrorCode.INVALID_INPUT));
	}

	@Test
	void payoutSettlement_returnsFalseOnDuplicateInsert() {
		Long memberId = 2L;
		Long settlementId = 20L;
		BigDecimal amount = new BigDecimal("500.00");
		MemberWallet wallet = MemberWallet.create(memberId);

		when(walletTransactionRepository.existsByRelatedSettlementIdAndTxType(
				settlementId, WalletTransactionType.SETTLEMENT_PAYOUT
		)).thenReturn(false);
		when(memberWalletRepository.findByMemberId(memberId)).thenReturn(Optional.of(wallet));
		when(walletTransactionRepository.save(any(WalletTransaction.class)))
				.thenThrow(new DataIntegrityViolationException("dup"));

		boolean processed = walletCommandService.payoutSettlement(memberId, settlementId, amount);

		assertThat(processed).isFalse();
		verify(walletTransactionRepository).save(any(WalletTransaction.class));
	}
}
