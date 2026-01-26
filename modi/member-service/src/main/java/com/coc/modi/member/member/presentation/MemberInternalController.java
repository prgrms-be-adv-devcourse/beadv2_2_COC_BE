package com.coc.modi.member.member.presentation;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.coc.modi.member.member.application.MemberService;
import com.coc.modi.member.member.application.dto.InternalAdminMemberCreateResponse;
import com.coc.modi.member.member.application.dto.MemberEmailResponse;
import com.coc.modi.member.member.application.dto.MemberPageResponse;
import com.coc.modi.member.member.application.dto.MemberSummaryResponse;
import com.coc.modi.member.member.domain.MemberStatus;
import com.coc.modi.member.member.presentation.dto.InternalAdminMemberCreateRequest;
import com.coc.modi.member.member.presentation.dto.MemberAuthzResponse;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/members")
public class MemberInternalController {
	
	private final MemberService memberService;
	
	@PatchMapping("/{memberId}/role")
	public String changeMemberRole(@PathVariable("memberId") Long memberId) {
		
		return memberService.updateRoleToSeller(memberId);
	}

	@PatchMapping("/{memberId}/status")
	public void changeMemberStatus(@PathVariable("memberId") Long memberId,
								   @RequestParam("status") MemberStatus status) {

		memberService.updateStatus(memberId, status);
	}

	@GetMapping("/{memberId}/authz")
	public MemberAuthzResponse getMemberAuthz(@PathVariable("memberId") Long memberId) {

		List<String> roles = memberService.getMemberRoles(memberId);
		return new MemberAuthzResponse(memberId, roles);
	}
	
	@GetMapping("/{memberId}/email")
	public MemberEmailResponse getMemberEmail(@PathVariable("memberId") Long memberId) {
		
		return memberService.getMemberEmail(memberId);
	}

	@GetMapping
	public MemberPageResponse getMembers(Pageable pageable) {

		return memberService.getMemberPage(pageable);
	}

	@GetMapping("/{memberId:\\d+}")
	public MemberSummaryResponse getMember(@PathVariable("memberId") Long memberId) {

		return memberService.getMemberSummary(memberId);
	}

	@GetMapping("/search")
	public MemberSummaryResponse searchByEmail(@RequestParam("email") String email) {

		return memberService.getMemberSummaryByEmail(email);
	}

	@PostMapping("/batch")
	public List<MemberSummaryResponse> getMembersByIds(@RequestBody List<Long> memberIds) {

		return memberService.getMembersByIds(memberIds);
	}

	@PostMapping("/admin")
	public InternalAdminMemberCreateResponse createAdmin(
			@Valid @RequestBody InternalAdminMemberCreateRequest request
	) {

		return memberService.createAdmin(request.toCommand());
	}
}
