package com.coc.modi.seller.seller.infrastructure.client.member;

import org.springframework.cloud.openfeign.FeignClient;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.coc.modi.seller.seller.infrastructure.client.member.dto.MemberEmailResponse;

@FeignClient(
        name = "memberFeignClient",
        url = "${member-service.url}",
        configuration = MemberFeignClientConfig.class
)
public interface MemberFeignClient {

    @PatchMapping("/internal/members/{memberId}/role")
	String changeMemberRole(@PathVariable("memberId") Long memberId);

    @GetMapping("/internal/members/{memberId}/email")
	MemberEmailResponse getMemberEmail(@PathVariable("memberId") Long memberId);
}
