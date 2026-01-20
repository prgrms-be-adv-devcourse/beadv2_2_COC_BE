package com.coc.modi.member.member.presentation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coc.modi.member.member.application.MemberService;
import com.coc.modi.member.member.application.dto.MemberEmailResponse;

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
	
	@GetMapping("/{memberId}/email")
	public MemberEmailResponse getMemberEmail(@PathVariable("memberId") Long memberId) {
		
		return memberService.getMemberEmail(memberId);
	}
}
