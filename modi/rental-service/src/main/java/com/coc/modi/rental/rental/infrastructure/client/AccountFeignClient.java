package com.coc.modi.rental.rental.infrastructure.client;

import com.coc.modi.rental.rental.infrastructure.client.dto.ChargeWalletCommand;
import com.coc.modi.rental.rental.infrastructure.client.dto.RefundWalletCommand;
import com.coc.modi.rental.rental.infrastructure.client.dto.WalletInfoResponse;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "account-service",
		url = "${account-service.url}",
		path = "/internal/wallets"
)
public interface AccountFeignClient {
	
	@PostMapping("/rental-payment")
	WalletInfoResponse charge(@RequestBody ChargeWalletCommand command);
	
	@PostMapping("/rental-refund")
	WalletInfoResponse refund(@RequestBody RefundWalletCommand command);
}
