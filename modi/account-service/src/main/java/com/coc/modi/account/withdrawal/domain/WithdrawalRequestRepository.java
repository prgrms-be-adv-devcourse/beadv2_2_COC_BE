package com.coc.modi.account.withdrawal.domain;

import java.util.List;
import java.util.Optional;

public interface WithdrawalRequestRepository {

    WithdrawalRequest save(WithdrawalRequest request);

    Optional<WithdrawalRequest> findById(Long id);

    List<WithdrawalRequest> findByStatus(WithdrawalStatus status);
}
