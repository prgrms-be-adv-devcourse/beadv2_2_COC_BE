package com.coc.modi.member.member.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.member.member.application.MemberService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/members")
public class MemberInternalController {
	
	private final MemberService memberService;
	
	@PatchMapping("/{memberId}/role")
	public ResponseEntity<ApiResponse<Void>> changeMemberRole(@PathVariable("memberId") Long memberId) {
	
		memberService.updateRole(memberId);
		
		return ResponseEntity.ok(ApiResponse.ok(null));
	}
}
