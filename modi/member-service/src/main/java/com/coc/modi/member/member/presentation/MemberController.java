package com.coc.modi.member.member.presentation;

import com.coc.modi.member.member.application.MemberService;
import com.coc.modi.member.member.application.dto.MemberProfileResponse;
import com.coc.modi.member.member.application.dto.MemberSignupResponse;
import com.coc.modi.member.member.presentation.dto.MemberSignupRequest;
import com.coc.modi.member.member.presentation.dto.MemberUpdateRequest;
import com.coc.modi.member.member.presentation.dto.MemberPasswordUpdateRequest;
import com.coc.modi.common.ApiResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {
	
	private final MemberService memberService;
	
	// 회원가입
	@PostMapping("/signup")
	public ResponseEntity<ApiResponse<MemberSignupResponse>> signup(@RequestBody MemberSignupRequest request) {
		
		MemberSignupResponse response = memberService.signup(request.toCommand());
		
		return ResponseEntity.ok(ApiResponse.ok(response));
	}
	
	// 내 정보 조회
	@GetMapping("/profile")
	public ResponseEntity<ApiResponse<MemberProfileResponse>> getProfile(Authentication authentication) {
		
		Long memberId = (Long)authentication.getPrincipal();
		MemberProfileResponse response = memberService.getProfile(memberId);
		
		return ResponseEntity.ok(ApiResponse.ok(response));
	}
	
	// 내 정보 수정
	@PutMapping("/profile")
	public ResponseEntity<ApiResponse<MemberProfileResponse>> updateProfile(Authentication authentication,
																			@RequestBody MemberUpdateRequest request) {
		
		Long memberId = (Long)authentication.getPrincipal();
		MemberProfileResponse response = memberService.updateProfile(memberId, request.toCommand());
		
		return ResponseEntity.ok(ApiResponse.ok(response));
	}
	
	// 비밀번호 수정
	@PatchMapping("/{memberId}/passwords")
	public ResponseEntity<ApiResponse<?>> updatePassword(Authentication authentication,
														 @PathVariable Long memberId,
														 @RequestBody MemberPasswordUpdateRequest request) {
		
		Long authenticatedMemberId = (Long)authentication.getPrincipal();
		memberService.updatePassword(authenticatedMemberId, memberId, request.toCommand());
		
		return ResponseEntity.ok(ApiResponse.ok());
	}
	
	// 회원 탈퇴
	@DeleteMapping
	public ResponseEntity<ApiResponse<?>> deleteMember(Authentication authentication) {
		
		Long memberId = (Long)authentication.getPrincipal();
		memberService.deleteMember(memberId);
		
		return ResponseEntity.ok(ApiResponse.ok());
	}
}
