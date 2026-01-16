package com.coc.modi.account.deposit.domain;

import java.util.List;
import java.util.Optional;

public interface PgDepositRepository {

    PgDeposit save(PgDeposit pgDeposit);

    Optional<PgDeposit> findById(Long id);

    Optional<PgDeposit> findByPgTid(String pgTid);

    Optional<PgDeposit> findByPgTidForUpdate(String pgTid);

    List<PgDeposit> findByMemberId(Long memberId);

    List<PgDeposit> findByMemberIdAndStatus(Long memberId, PgDepositStatus status);
}
