package com.coc.modi.admin.infrastructure.client.member;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PatchMapping;

import com.coc.modi.admin.application.dto.AdminMemberCreateResponse;
import com.coc.modi.admin.infrastructure.client.AdminFeignClientConfig;
import com.coc.modi.admin.infrastructure.client.member.dto.AdminMemberCreateInternalRequest;
import com.coc.modi.admin.infrastructure.client.member.dto.MemberPageRequest;
import com.coc.modi.admin.infrastructure.client.member.dto.MemberPageResponse;
import com.coc.modi.admin.infrastructure.client.member.dto.MemberSummaryResponse;

@FeignClient(
		name = "memberAdminClient",
		url = "${member-service.url}",
		configuration = AdminFeignClientConfig.class
)
public interface MemberAdminClient {

	@GetMapping("/internal/members")
	MemberPageResponse getMembers(@SpringQueryMap MemberPageRequest request);

	@GetMapping("/internal/members/{memberId}")
	MemberSummaryResponse getMember(@PathVariable("memberId") Long memberId);

	@GetMapping("/internal/members/search")
	MemberSummaryResponse searchByEmail(@RequestParam("email") String email);

	@PostMapping("/internal/members/batch")
	List<MemberSummaryResponse> getMembersByIds(@RequestBody List<Long> memberIds);

	@PostMapping("/internal/members/admin")
	AdminMemberCreateResponse createAdmin(@RequestBody AdminMemberCreateInternalRequest request);

	@PatchMapping("/internal/members/{memberId}/status")
	void updateMemberStatus(
			@PathVariable("memberId") Long memberId,
			@RequestParam("status") String status
	);
}
