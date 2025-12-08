package com.coc.modi.account.wallet.infrastructure;

import com.coc.modi.account.wallet.domain.MemberWallet;
import com.coc.modi.account.wallet.domain.MemberWalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MemberWalletRepositoryAdapter implements MemberWalletRepository {

    private final MemberWalletJpaRepository jpaRepository;

    @Override
    public Optional<MemberWallet> findById(Long memberId) {
        return jpaRepository.findById(memberId);
    }

    @Override
    public Optional<MemberWallet> findByMemberId(Long memberId) {
        return jpaRepository.findByMemberId(memberId);
    }

    @Override
    public MemberWallet save(MemberWallet wallet) {
        return jpaRepository.save(wallet);
    }
}
