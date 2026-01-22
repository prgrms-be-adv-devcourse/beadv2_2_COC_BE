package com.coc.modi.account.deposit.infrastructure;

import com.coc.modi.account.deposit.domain.PgDeposit;
import com.coc.modi.account.deposit.domain.PgDepositRepository;
import com.coc.modi.account.deposit.domain.PgDepositStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PgDepositRepositoryAdapter implements PgDepositRepository {

    private final PgDepositJpaRepository pgDepositJpaRepository;

    @Override
    public PgDeposit save(PgDeposit pgDeposit) {

        return pgDepositJpaRepository.save(pgDeposit);
    }

    @Override
    public Optional<PgDeposit> findById(Long id) {

        return pgDepositJpaRepository.findById(id);
    }

    @Override
    public Optional<PgDeposit> findByPgTid(String pgTid) {

        return pgDepositJpaRepository.findByPgTid(pgTid);
    }

    @Override
    public Optional<PgDeposit> findByPgTidForUpdate(String pgTid) {

        return pgDepositJpaRepository.findByPgTidForUpdate(pgTid);
    }

    @Override
    public List<PgDeposit> findByMemberId(Long memberId) {

        return pgDepositJpaRepository.findByMemberId(memberId);
    }

    @Override
    public List<PgDeposit> findByMemberIdAndStatus(Long memberId, PgDepositStatus status) {

        return pgDepositJpaRepository.findByMemberIdAndStatus(memberId, status);
    }

    @Override
    public List<PgDeposit> findAllocatableByMemberId(Long memberId) {

        return pgDepositJpaRepository.findAllocatableByMemberId(memberId, PgDepositStatus.SUCCESS);
    }
}
