package com.coc.modi.member.auth.presentation;

import com.coc.modi.member.auth.application.MemberAuthService;
import com.coc.modi.member.auth.application.EmailVerificationService;
import com.coc.modi.member.auth.application.dto.MemberLoginResponse;
import com.coc.modi.member.auth.presentation.dto.*;
import com.coc.modi.common.ApiResponse;
import com.coc.modi.member.auth.presentation.dto.EmailVerificationConfirmRequest;
import com.coc.modi.member.auth.presentation.dto.EmailVerificationConfirmResponse;
import com.coc.modi.member.auth.presentation.dto.EmailVerificationSendRequest;
import com.coc.modi.member.auth.presentation.dto.EmailVerificationSendResponse;
import com.coc.modi.member.auth.presentation.dto.MemberLoginRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class MemberAuthController {
	
	private final MemberAuthService memberAuthService;
	private final EmailVerificationService emailVerificationService;
	
	// 로그인
	@PostMapping("/login")
	public ResponseEntity<ApiResponse<MemberLoginResponse>> login(@RequestBody MemberLoginRequest request) {
		
		MemberLoginResponse response = memberAuthService.login(request.toCommand());
		
		return ResponseEntity.ok(ApiResponse.ok(response));
	}
	
	// 이메일 인증 코드 발송
	@PostMapping("/email/verify/send")
	public ResponseEntity<ApiResponse<EmailVerificationSendResponse>> sendEmailVerification(@RequestBody EmailVerificationSendRequest request) {
		
		EmailVerificationSendResponse response = emailVerificationService.sendVerificationEmail(request.toCommand());
		
		return ResponseEntity.ok(ApiResponse.ok(response));
	}
	
	// 이메일 인증 코드 검증
	@PostMapping("/email/verify/confirm")
	public ResponseEntity<ApiResponse<EmailVerificationConfirmResponse>> confirmEmailVerification(@RequestBody EmailVerificationConfirmRequest request) {
		
		EmailVerificationConfirmResponse response = emailVerificationService.confirmVerification(request.toCommand());
		
		return ResponseEntity.ok(ApiResponse.ok(response));
	}
}
