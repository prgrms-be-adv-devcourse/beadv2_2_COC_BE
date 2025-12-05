package com.coc.modi.rental.infrastructure.client;

import com.coc.modi.rental.infrastructure.client.dto.ChargeWalletCommand;
import com.coc.modi.rental.infrastructure.client.dto.WalletInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "account-service",
        url = "${account-service.url}",
        path = "/internal/accounts"
)
public interface AccountFeignClient {

    @PostMapping("/wallets")
    WalletInfoResponse charge(@RequestBody ChargeWalletCommand command);
}
