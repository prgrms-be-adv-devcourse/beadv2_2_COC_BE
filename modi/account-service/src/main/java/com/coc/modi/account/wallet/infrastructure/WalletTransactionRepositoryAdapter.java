package com.coc.modi.account.wallet.infrastructure;

import com.coc.modi.account.wallet.domain.WalletTransaction;
import com.coc.modi.account.wallet.domain.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class WalletTransactionRepositoryAdapter implements WalletTransactionRepository {

    private final WalletTransactionJpaRepository jpaRepository;

    @Override
    public WalletTransaction save(WalletTransaction tx) {

        return jpaRepository.save(tx);
    }

    @Override
    public List<WalletTransaction> findByMemberId(Long memberId) {

        return jpaRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
    }
}
