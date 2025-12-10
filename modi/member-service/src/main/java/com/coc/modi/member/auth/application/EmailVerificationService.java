package com.coc.modi.member.auth.application;

import com.coc.modi.member.auth.application.dto.ConfirmEmailVerificationCommand;
import com.coc.modi.member.auth.application.dto.SendEmailVerificationCommand;
import com.coc.modi.member.auth.infrastructure.EmailVerificationCodeStore;
import com.coc.modi.member.auth.infrastructure.mail.EmailSender;
import com.coc.modi.member.auth.presentation.dto.EmailVerificationSendResponse;
import com.coc.modi.member.auth.presentation.dto.EmailVerificationConfirmResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {
	
	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
	private static final Pattern CODE_PATTERN = Pattern.compile("^\\d{6}$");
	private static final int CODE_BOUND = 1_000_000;
	private static final long EXPIRATION_MINUTES = 5L;
	
	private final EmailVerificationCodeStore emailVerificationCodeStore;
	private final SecureRandom secureRandom = new SecureRandom();
	private final EmailSender emailSender;
	
	// 이메일 인증 코드 발송
	@Transactional
	public EmailVerificationSendResponse sendVerificationEmail(SendEmailVerificationCommand command) {
		
		validateEmail(command.email());
		
		String code = generateCode();
		
		emailVerificationCodeStore.saveCode(command.email(), code, Duration.ofMinutes(EXPIRATION_MINUTES));
		
		emailSender.sendVerificationCode(command.email(), code);
		
		return EmailVerificationSendResponse.success();
	}
	
	// 이메일 인증 코드 검증
	@Transactional
	public EmailVerificationConfirmResponse confirmVerification(ConfirmEmailVerificationCommand command) {
		
		validateEmail(command.email());
		validateCode(command.code());
		
		String storedCode = emailVerificationCodeStore.getCode(command.email());
		
		if (storedCode == null) {
			
			throw new IllegalArgumentException("이메일 인증 요청이 존재하지 않습니다.");
		}
		
		if (!storedCode.equals(command.code())) {
			
			throw new IllegalArgumentException("인증 코드가 일치하지 않습니다.");
		}
		
		emailVerificationCodeStore.deleteCode(command.email());
		
		return EmailVerificationConfirmResponse.success();
	}
	
	// 이메일 인증 코드 생성
	private String generateCode() {
		
		return String.format("%06d", secureRandom.nextInt(CODE_BOUND));
	}
	
	// 이메일 유효성 검사
	private void validateEmail(String email) {
		
		if (email == null || email.isBlank()) {
			
			throw new IllegalArgumentException("이메일을 입력해주세요.");
		}
		
		if (!EMAIL_PATTERN.matcher(email).matches()) {
			
			throw new IllegalArgumentException("이메일 형식이 올바르지 않습니다.");
		}
	}
	
	// 인증 코드 유효성 검사
	private void validateCode(String code) {
		
		if (code == null || code.isBlank()) {
			
			throw new IllegalArgumentException("인증 코드를 입력해주세요.");
		}
		
		if (!CODE_PATTERN.matcher(code).matches()) {
			
			throw new IllegalArgumentException("인증 코드는 6자리 숫자입니다.");
		}
	}
}
