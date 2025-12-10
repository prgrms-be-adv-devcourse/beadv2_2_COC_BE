package com.coc.modi.account.deposit.infrastructure;

import com.coc.modi.account.deposit.domain.PgDeposit;
import com.coc.modi.account.deposit.domain.PgDepositStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PgDepositJpaRepository extends JpaRepository<PgDeposit, Long> {

    Optional<PgDeposit> findByPgTid(String pgTid);

    List<PgDeposit> findByMemberId(Long memberId);

    List<PgDeposit> findByMemberIdAndStatus(Long memberId, PgDepositStatus status);

    List<PgDeposit> findByMemberIdOrderByCreatedAtDesc(Long memberId);

}
