package com.coc.modi.seller.settlement.infrastructure.client.wallet;

import com.coc.modi.seller.config.FeignConfig;
import com.coc.modi.seller.settlement.infrastructure.client.wallet.dto.SettlementPayoutRequest;
import com.coc.modi.seller.settlement.infrastructure.client.wallet.dto.SettlementPayoutResponse;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "account-service",
        url = "${account-service.url}",
        configuration = FeignConfig.class,
        path = "/internal/wallets"
)
public interface WalletFeignClient {

    @PostMapping("/settlement-payout")
    SettlementPayoutResponse payoutSettlement(@RequestBody SettlementPayoutRequest request);
}
