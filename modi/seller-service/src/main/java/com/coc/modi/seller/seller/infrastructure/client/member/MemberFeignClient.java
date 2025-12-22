package com.coc.modi.seller.seller.infrastructure.client.member;

import org.springframework.cloud.openfeign.FeignClient;

import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;


import com.coc.modi.seller.config.FeignConfig;

@FeignClient(name = "memberFeignClient", url = "${member-service.url}", configuration = FeignConfig.class)
public interface MemberFeignClient {

    @PatchMapping("/internal/members/{memberId}/role")
	String changeMemberRole(@PathVariable("memberId") Long memberId);
}
