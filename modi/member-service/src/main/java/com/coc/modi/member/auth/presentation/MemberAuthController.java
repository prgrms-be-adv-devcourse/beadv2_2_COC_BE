package com.coc.modi.member.auth.presentation;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.member.auth.application.EmailVerificationService;
import com.coc.modi.member.auth.application.MemberAuthService;
import com.coc.modi.member.auth.application.PasswordResetService;
import com.coc.modi.member.auth.application.dto.LogoutResponse;
import com.coc.modi.member.auth.application.dto.MemberLoginResponse;
import com.coc.modi.member.auth.application.dto.TokenReissueResponse;
import com.coc.modi.member.auth.presentation.dto.EmailVerificationConfirmRequest;
import com.coc.modi.member.auth.application.dto.EmailVerificationConfirmResponse;
import com.coc.modi.member.auth.presentation.dto.EmailVerificationSendRequest;
import com.coc.modi.member.auth.application.dto.EmailVerificationSendResponse;
import com.coc.modi.member.auth.presentation.dto.MemberLoginRequest;
import com.coc.modi.member.auth.presentation.dto.OAuth2ConnectRequest;
import com.coc.modi.member.auth.presentation.dto.OAuth2SignupRequest;
import com.coc.modi.member.auth.presentation.dto.PasswordResetConfirmRequest;
import com.coc.modi.member.auth.presentation.dto.PasswordResetRequest;
import com.coc.modi.member.auth.oauth2.OAuth2AuthService;
import com.coc.modi.common.auth.CustomMember;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
	private final OAuth2AuthService oauth2AuthService;
	
	// 로그인
	@PostMapping("/login")
	public ResponseEntity<ApiResponse<?>> login(@Valid @RequestBody MemberLoginRequest request,
												HttpServletRequest httpServletRequest) {
		
		boolean secureCookie = httpServletRequest.isSecure();
		MemberLoginResponse response = memberAuthService.login(request.toCommand(), secureCookie);
		
		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, response.refreshCookie().toString())
				.body(ApiResponse.ok(response.accessToken()));
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
	
	@PostMapping("/reissue")
	public ResponseEntity<ApiResponse<?>> reissue(HttpServletRequest request) {
		
		boolean secureCookie = request.isSecure();
		TokenReissueResponse response = memberAuthService.reissue(request, secureCookie);
		
		return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, response.responseCookie().toString())
				.body(ApiResponse.ok(response.accessToken()));
	}
	
	
	@PostMapping("/logout")
	public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest servletRequest) {
		
		boolean secureCookie = servletRequest.isSecure();
		LogoutResponse response = memberAuthService.logout(servletRequest, secureCookie);
		
		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, response.clear().toString())
				.body(ApiResponse.ok(null));
	}
	
	// OAuth2 회원가입
	@PostMapping("/oauth2/signup")
	public ResponseEntity<ApiResponse<?>> oauth2Signup(@Valid @RequestBody OAuth2SignupRequest request,
													   HttpServletRequest httpServletRequest) {

		boolean secureCookie = httpServletRequest.isSecure();
		MemberLoginResponse response = oauth2AuthService.signup(request.toCommand(), secureCookie);

		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, response.refreshCookie().toString())
				.body(ApiResponse.ok(response.accessToken()));
	}
	
	// OAuth2 계정 연결
	@PostMapping("/oauth2/connect")
	public ResponseEntity<ApiResponse<Void>> oauth2Connect(@AuthenticationPrincipal CustomMember member,
														   @Valid @RequestBody OAuth2ConnectRequest request) {

		oauth2AuthService.connect(member.memberId(), request.toCommand());
		return ResponseEntity.ok(ApiResponse.ok(null));
	}
}
