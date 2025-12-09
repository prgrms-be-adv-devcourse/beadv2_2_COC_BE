package com.coc.modi.auth.application;

import com.coc.modi.auth.application.dto.ConfirmEmailVerificationCommand;
import com.coc.modi.auth.application.dto.SendEmailVerificationCommand;
import com.coc.modi.auth.domain.EmailVerification;
import com.coc.modi.auth.domain.EmailVerificationRepository;
import com.coc.modi.auth.infrastructure.mail.EmailSender;
import com.coc.modi.auth.presentation.dto.EmailVerificationSendResponse;
import com.coc.modi.auth.presentation.dto.EmailVerificationConfirmResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {
	
	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
	private static final Pattern CODE_PATTERN = Pattern.compile("^\\d{6}$");
	private static final int CODE_BOUND = 1_000_000;
	private static final long EXPIRATION_MINUTES = 5L;
	
	private final EmailVerificationRepository emailVerificationRepository;
	private final SecureRandom secureRandom = new SecureRandom();
	private final EmailSender emailSender;
	
	// 이메일 인증 코드 발송
	@Transactional
	public EmailVerificationSendResponse sendVerificationEmail(SendEmailVerificationCommand command) {
		
		validateEmail(command.email());
		
		String code = generateCode();
		LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES);
		
		emailVerificationRepository.findByEmail(command.email())
				.ifPresentOrElse(existing -> existing.regenerate(code, expiresAt),
						() -> emailVerificationRepository.save(
								EmailVerification.create(command.email(), code, expiresAt)
						));
		
		emailSender.sendVerificationCode(command.email(), code);
		
		return EmailVerificationSendResponse.success();
	}
	
	// 이메일 인증 코드 검증
	@Transactional
	public EmailVerificationConfirmResponse confirmVerification(ConfirmEmailVerificationCommand command) {
		
		validateEmail(command.email());
		validateCode(command.code());
		
		EmailVerification emailVerification = emailVerificationRepository.findByEmail(command.email())
				.orElseThrow(() -> new IllegalArgumentException("이메일 인증 요청이 존재하지 않습니다."));
		
		emailVerification.verify(command.code(), LocalDateTime.now());
		
		return EmailVerificationConfirmResponse.success();
	}
	
	private String generateCode() {
		
		return String.format("%06d", secureRandom.nextInt(CODE_BOUND));
	}
	
	private void validateEmail(String email) {
		
		if (email == null || email.isBlank()) {
			
			throw new IllegalArgumentException("이메일을 입력해주세요.");
		}
		
		if (!EMAIL_PATTERN.matcher(email).matches()) {
			
			throw new IllegalArgumentException("이메일 형식이 올바르지 않습니다.");
		}
	}
	
	private void validateCode(String code) {
		
		if (code == null || code.isBlank()) {
			
			throw new IllegalArgumentException("인증 코드를 입력해주세요.");
		}
		
		if (!CODE_PATTERN.matcher(code).matches()) {
			
			throw new IllegalArgumentException("인증 코드는 6자리 숫자입니다.");
		}
	}
}
