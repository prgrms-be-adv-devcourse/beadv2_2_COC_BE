package com.coc.modi.member.member.presentation;

import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coc.modi.member.member.application.MemberService;

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
}
