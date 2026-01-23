package com.coc.modi.account.withdrawal.infrastructure;

import com.coc.modi.account.withdrawal.domain.WithdrawalRequest;
import com.coc.modi.account.withdrawal.domain.WithdrawalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WithdrawalRequestJpaRepository extends JpaRepository<WithdrawalRequest, Long> {

    List<WithdrawalRequest> findByStatus(WithdrawalStatus status);
}
