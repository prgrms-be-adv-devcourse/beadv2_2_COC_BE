package com.coc.modi.seller.settlement.infrastructure.client.wallet;

import com.coc.modi.common.ErrorCode;
import com.coc.modi.seller.exception.SellerException;
import com.coc.modi.seller.settlement.infrastructure.client.wallet.dto.SettlementPayoutRequest;
import com.coc.modi.seller.settlement.infrastructure.client.wallet.dto.SettlementPayoutResponse;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WalletClientAdapter {

    private final WalletFeignClient walletFeignClient;

    @Retry(name = "settlementPayoutRetry")
    @CircuitBreaker(name = "settlementPayoutCircuitBreaker", fallbackMethod = "fallbackPayoutSettlement")
    public SettlementPayoutResponse payoutSettlement(SettlementPayoutRequest request) {

        return walletFeignClient.payoutSettlement(request);
    }

    private SettlementPayoutResponse fallbackPayoutSettlement(SettlementPayoutRequest request, Throwable throwable) {

        Long memberId = request != null ? request.memberId() : null;
        Long settlementId = request != null ? request.settlementId() : null;
        log.warn("정산 지급 요청 실패 memberId={}, settlementId={}", memberId, settlementId, throwable);
        throw new SellerException(ErrorCode.INTERNAL_ERROR, "지갑 서비스 호출에 실패했습니다.");
    }
}
