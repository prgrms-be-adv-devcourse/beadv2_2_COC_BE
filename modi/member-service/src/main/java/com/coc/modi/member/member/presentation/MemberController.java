package com.coc.modi.member.member.presentation;

import com.coc.modi.common.auth.CustomMember;
import com.coc.modi.member.member.application.MemberService;
import com.coc.modi.member.member.application.dto.MemberProfileResponse;
import com.coc.modi.member.member.application.dto.MemberSignupResponse;
import com.coc.modi.member.member.presentation.dto.MemberSignupRequest;
import com.coc.modi.member.member.presentation.dto.MemberUpdateRequest;
import com.coc.modi.member.member.presentation.dto.MemberPasswordUpdateRequest;
import com.coc.modi.common.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {
	
	private final MemberService memberService;
	
	// 회원가입
	@PostMapping("/signup")
	public ResponseEntity<ApiResponse<MemberSignupResponse>> signup(@Valid @RequestBody MemberSignupRequest request) {
		
		MemberSignupResponse response = memberService.signup(request.toCommand());
		
		return ResponseEntity.ok(ApiResponse.ok(response));
	}
	
	// 내 정보 조회
	@GetMapping("/profile")
	public ResponseEntity<ApiResponse<MemberProfileResponse>> getProfile(@AuthenticationPrincipal CustomMember member) {
		
		MemberProfileResponse response = memberService.getProfile(member.getMemberId());
		
		return ResponseEntity.ok(ApiResponse.ok(response));
	}
	
	// 내 정보 수정
	@PutMapping("/profile")
	public ResponseEntity<ApiResponse<MemberProfileResponse>> updateProfile(@AuthenticationPrincipal CustomMember member,
																			@Valid @RequestBody MemberUpdateRequest request) {
		
		MemberProfileResponse response = memberService.updateProfile(member.getMemberId(), request.toCommand());
		
		return ResponseEntity.ok(ApiResponse.ok(response));
	}
	
	// 비밀번호 수정
	@PatchMapping("/{memberId}/passwords")
	public ResponseEntity<ApiResponse<Void>> updatePassword(Authentication authentication,
															@PathVariable Long memberId,
															@Valid @RequestBody MemberPasswordUpdateRequest request) {
		
		Long authenticatedMemberId = (Long)authentication.getPrincipal();
		
		memberService.updatePassword(authenticatedMemberId, memberId, request.toCommand());
		
		return ResponseEntity.ok(ApiResponse.ok(null));
	}
	
	// 회원 탈퇴
	@DeleteMapping
	public ResponseEntity<ApiResponse<Void>> deleteMember(@AuthenticationPrincipal CustomMember member) {
		
		memberService.deleteMember(member.getMemberId());
		
		return ResponseEntity.ok(ApiResponse.ok(null));
	}
}
