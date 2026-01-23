package com.coc.modi.notification.infrastructure.client.member;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.coc.modi.notification.infrastructure.client.member.dto.MemberEmailResponse;

@FeignClient(
		name = "memberFeignClient",
		url = "${member-service.url}",
		configuration = MemberFeignClientConfig.class
)
public interface MemberFeignClient {

	@GetMapping("/internal/members/{memberId}/email")
	MemberEmailResponse getMemberEmail(@PathVariable("memberId") Long memberId);
}
