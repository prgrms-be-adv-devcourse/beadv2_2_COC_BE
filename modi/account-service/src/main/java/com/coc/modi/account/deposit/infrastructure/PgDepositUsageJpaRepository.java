package com.coc.modi.account.deposit.infrastructure;

import com.coc.modi.account.deposit.domain.PgDepositUsage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PgDepositUsageJpaRepository extends JpaRepository<PgDepositUsage, Long> {
}
