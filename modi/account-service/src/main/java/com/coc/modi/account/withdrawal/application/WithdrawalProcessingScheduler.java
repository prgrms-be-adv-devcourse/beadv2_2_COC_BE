package com.coc.modi.account.withdrawal.application;

import com.coc.modi.account.withdrawal.domain.WithdrawalRequest;
import com.coc.modi.account.withdrawal.domain.WithdrawalRequestRepository;
import com.coc.modi.account.withdrawal.domain.WithdrawalStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WithdrawalProcessingScheduler {

    private final WithdrawalRequestRepository withdrawalRequestRepository;

    @Scheduled(fixedDelayString = "${withdrawal.processing.interval-ms:5000}")
    @Transactional
    public void processProcessingRequests() {

        List<WithdrawalRequest> requests = withdrawalRequestRepository.findByStatus(WithdrawalStatus.PROCESSING);
        for (WithdrawalRequest request : requests) {
            request.markCompleted();
        }
    }
}
