package com.coc.modi.account.deposit.infrastructure;

import com.coc.modi.account.deposit.domain.PgDepositUsage;
import com.coc.modi.account.deposit.domain.PgDepositUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PgDepositUsageRepositoryAdapter implements PgDepositUsageRepository {

    private final PgDepositUsageJpaRepository pgDepositUsageJpaRepository;

    @Override
    public PgDepositUsage save(PgDepositUsage usage) {

        return pgDepositUsageJpaRepository.save(usage);
    }
}
