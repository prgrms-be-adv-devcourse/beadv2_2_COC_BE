package com.coc.modi.account.wallet.infrastructure;

import com.coc.modi.account.wallet.domain.MemberWallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberWalletJpaRepository extends JpaRepository<MemberWallet, Long> {

    Optional<MemberWallet> findByMemberId(Long memberId);

}
