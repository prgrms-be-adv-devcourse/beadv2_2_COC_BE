package com.coc.modi.member.auth.presentation;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.member.auth.application.EmailVerificationService;
import com.coc.modi.member.auth.application.MemberAuthService;
import com.coc.modi.member.auth.application.PasswordResetService;
import com.coc.modi.member.auth.application.dto.MemberLoginResponse;
import com.coc.modi.member.auth.presentation.dto.EmailVerificationConfirmRequest;
import com.coc.modi.member.auth.application.dto.EmailVerificationConfirmResponse;
import com.coc.modi.member.auth.presentation.dto.EmailVerificationSendRequest;
import com.coc.modi.member.auth.application.dto.EmailVerificationSendResponse;
import com.coc.modi.member.auth.presentation.dto.MemberLoginRequest;
import com.coc.modi.member.auth.presentation.dto.PasswordResetConfirmRequest;
import com.coc.modi.member.auth.presentation.dto.PasswordResetRequest;

import jakarta.validation.Valid;
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
	private final PasswordResetService passwordResetService;
	
	// 로그인
	@PostMapping("/login")
	public ResponseEntity<ApiResponse<MemberLoginResponse>> login(@Valid @RequestBody MemberLoginRequest request) {
		
		MemberLoginResponse response = memberAuthService.login(request.toCommand());
		
		return ResponseEntity.ok(ApiResponse.ok(response));
	}
	
	// 이메일 인증 코드 발송
	@PostMapping("/email/verify/send")
	public ResponseEntity<ApiResponse<EmailVerificationSendResponse>> sendEmailVerification(@Valid @RequestBody EmailVerificationSendRequest request) {
		
		EmailVerificationSendResponse response = emailVerificationService.sendVerificationEmail(request.toCommand());
		
		return ResponseEntity.ok(ApiResponse.ok(response));
	}
	
	// 이메일 인증 코드 검증
	@PostMapping("/email/verify/confirm")
	public ResponseEntity<ApiResponse<EmailVerificationConfirmResponse>> confirmEmailVerification(@Valid @RequestBody EmailVerificationConfirmRequest request) {
		
		EmailVerificationConfirmResponse response = emailVerificationService.confirmVerification(request.toCommand());
		
		return ResponseEntity.ok(ApiResponse.ok(response));
	}
	
	// 비밀번호 재설정 코드 발송
	@PostMapping("/password/reset/send")
	public ResponseEntity<ApiResponse<Void>> sendPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
		
		passwordResetService.sendResetCode(request.toCommand());
		
		return ResponseEntity.ok(ApiResponse.ok(null));
	}
	
	// 비밀번호 재설정
	@PostMapping("/password/reset/confirm")
	public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody PasswordResetConfirmRequest request) {
		
		passwordResetService.resetPassword(request.toCommand());
		
		return ResponseEntity.ok(ApiResponse.ok(null));
	}
}
