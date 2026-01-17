package com.coc.modi.account.withdrawal.infrastructure;

import com.coc.modi.account.withdrawal.domain.WithdrawalRequest;
import com.coc.modi.account.withdrawal.domain.WithdrawalRequestRepository;
import com.coc.modi.account.withdrawal.domain.WithdrawalStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class WithdrawalRequestRepositoryAdapter implements WithdrawalRequestRepository {

    private final WithdrawalRequestJpaRepository withdrawalRequestJpaRepository;

    @Override
    public WithdrawalRequest save(WithdrawalRequest request) {

        return withdrawalRequestJpaRepository.save(request);
    }

    @Override
    public Optional<WithdrawalRequest> findById(Long id) {

        return withdrawalRequestJpaRepository.findById(id);
    }

    @Override
    public List<WithdrawalRequest> findByStatus(WithdrawalStatus status) {

        return withdrawalRequestJpaRepository.findByStatus(status);
    }
}
