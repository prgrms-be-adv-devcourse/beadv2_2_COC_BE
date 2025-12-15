package com.coc.modi.seller.infrastructure.client.member;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.coc.modi.common.ApiResponse;

@FeignClient(name = "memberFeignClient", url = "${member-service.url}")
public interface MemberFeignClient {

    @PatchMapping("/internal/members/{memberId}/role")
	ResponseEntity<ApiResponse<Void>> changeMemberRole(@PathVariable("memberId") Long memberId);
	
}
