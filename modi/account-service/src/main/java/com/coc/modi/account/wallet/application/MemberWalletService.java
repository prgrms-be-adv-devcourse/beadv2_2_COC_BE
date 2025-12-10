package com.coc.modi.account.wallet.application;

import com.coc.modi.account.exception.AccountNotFoundException;
import com.coc.modi.account.wallet.application.dto.MemberWalletResponse;
import com.coc.modi.account.wallet.application.dto.WalletTransactionResponse;
import com.coc.modi.account.wallet.domain.MemberWallet;
import com.coc.modi.account.wallet.domain.MemberWalletRepository;
import com.coc.modi.account.wallet.domain.WalletTransactionRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberWalletService {

    private final MemberWalletRepository memberWalletRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    // 예치금 조회
    @Transactional(readOnly = true)
    public MemberWalletResponse getMemberWalletBalance(Long memberId) {

        MemberWallet wallet = memberWalletRepository.findByMemberId(memberId)
                .orElseThrow(() -> new AccountNotFoundException(memberId));

        return MemberWalletResponse.from(wallet);
    }

    // 예치금 거래 내역 조회
    @Transactional(readOnly = true)
    public List<WalletTransactionResponse> getWalletTransactions(Long memberId) {

        return walletTransactionRepository.findByMemberId(memberId)
                .stream()
                .map(WalletTransactionResponse::from)
                .collect(Collectors.toList());
    }
}
