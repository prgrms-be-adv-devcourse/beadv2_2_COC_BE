package com.coc.modi.account.wallet.infrastructure;

import com.coc.modi.account.wallet.domain.MemberWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

import jakarta.persistence.LockModeType;

public interface MemberWalletJpaRepository extends JpaRepository<MemberWallet, Long> {

    Optional<MemberWallet> findByMemberId(Long memberId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select m from MemberWallet m where m.memberId = :memberId")
	Optional<MemberWallet> findByMemberIdForUpdate(@Param("memberId") Long memberId);
}
