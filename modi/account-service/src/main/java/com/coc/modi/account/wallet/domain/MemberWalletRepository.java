package com.coc.modi.account.wallet.domain;

import java.util.Optional;

public interface MemberWalletRepository {

    Optional<MemberWallet> findById(Long memberId);

    Optional<MemberWallet> findByMemberId(Long memberId);

	Optional<MemberWallet> findByMemberIdForUpdate(Long memberId);

    MemberWallet save(MemberWallet wallet);
}
