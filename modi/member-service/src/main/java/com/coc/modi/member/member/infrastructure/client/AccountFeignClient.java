package com.coc.modi.member.member.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.coc.modi.member.member.infrastructure.client.dto.MemberWalletResponse;

@FeignClient(
		name = "account-service",
		url = "${account-service.url}",
		path = "/internal/wallets"
)
public interface AccountFeignClient {
	
	@PostMapping("/{memberId}")
	void createWallet(@PathVariable Long memberId);
	
	@GetMapping("/{memberId}/balance")
	MemberWalletResponse getWalletBalance(@PathVariable Long memberId);
}
