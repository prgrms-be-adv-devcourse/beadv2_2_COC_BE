package com.coc.modi.account.wallet.infrastructure;

import com.coc.modi.account.wallet.domain.MemberWallet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MemberWalletRepository {

    private final MemberWalletJpaRepository memberWalletJpaRepository;

    public Optional<MemberWallet> findById(Long memberId) {

        return memberWalletJpaRepository.findById(memberId);
    }
}
