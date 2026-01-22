package com.coc.modi.account.deposit.infrastructure;

import com.coc.modi.account.deposit.domain.PgDeposit;
import com.coc.modi.account.deposit.domain.PgDepositStatus;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

public interface PgDepositJpaRepository extends JpaRepository<PgDeposit, Long> {

    Optional<PgDeposit> findByPgTid(String pgTid);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PgDeposit p where p.pgTid = :pgTid")
    Optional<PgDeposit> findByPgTidForUpdate(@Param("pgTid") String pgTid);

    List<PgDeposit> findByMemberId(Long memberId);

    List<PgDeposit> findByMemberIdAndStatus(Long memberId, PgDepositStatus status);

    List<PgDeposit> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select p
            from PgDeposit p
            where p.memberId = :memberId
              and p.status = :status
              and p.remainingAmount > 0
            order by p.approvedAt asc, p.id asc
            """)
    List<PgDeposit> findAllocatableByMemberId(@Param("memberId") Long memberId,
                                              @Param("status") PgDepositStatus status);

}
