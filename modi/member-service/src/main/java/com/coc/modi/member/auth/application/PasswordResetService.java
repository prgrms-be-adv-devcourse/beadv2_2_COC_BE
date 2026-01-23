package com.coc.modi.member.auth.application;

import com.coc.modi.member.auth.application.dto.PasswordResetCommand;
import com.coc.modi.member.auth.application.dto.PasswordResetConfirmCommand;
import com.coc.modi.member.auth.application.dto.PasswordResetConfirmResponse;
import com.coc.modi.member.auth.application.dto.PasswordResetSendCommand;
import com.coc.modi.member.auth.infrastructure.PasswordResetCodeStore;
import com.coc.modi.member.auth.infrastructure.PasswordResetTokenStore;
import com.coc.modi.member.auth.infrastructure.mail.EmailSender;
import com.coc.modi.member.member.domain.Member;
import com.coc.modi.member.member.domain.MemberRepository;
import com.coc.modi.member.member.domain.MemberStatus;
import com.coc.modi.member.member.exception.AuthCodeInvalidException;
import com.coc.modi.member.member.exception.MemberNotFoundException;
import com.coc.modi.member.member.exception.MemberWithdrawnException;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {
	
	private static final int CODE_BOUND = 1_000_000;
	private static final Duration CODE_EXPIRATION = Duration.ofMinutes(5);
	private static final Duration TOKEN_EXPIRATION = Duration.ofMinutes(10);
	
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final PasswordResetCodeStore passwordResetCodeStore;
	private final PasswordResetTokenStore passwordResetTokenStore;
	private final EmailSender emailSender;
	private final SecureRandom secureRandom = new SecureRandom();
	
    @Transactional
    public void sendResetCode(PasswordResetSendCommand command) {
		
		Member member = memberRepository.findByEmail(command.email())
				.orElseThrow(() -> new MemberNotFoundException(command.email()));
		
		String code = generateCode();
		
		passwordResetCodeStore.saveCode(member.getEmail(), code, CODE_EXPIRATION);
		emailSender.sendVerificationCode(member.getEmail(), code);
		
    }

    // 비밀번호 재설정 코드 확인
    @Transactional
    public PasswordResetConfirmResponse confirmResetCode(PasswordResetConfirmCommand command) {
		
		String storedCode = passwordResetCodeStore.getCode(command.email());
		
		if (storedCode == null || !storedCode.equals(command.code())) {
			
			throw new AuthCodeInvalidException("비밀번호 재설정 코드가 일치하지 않습니다.");
		}
		
		Member member = memberRepository.findByEmail(command.email())
				.orElseThrow(() -> new MemberNotFoundException(command.email()));
		
		// 탈퇴한 회원인지 확인
		if (member.getStatus() == MemberStatus.WITHDRAWN) {
			
			// 인증코드 삭제
			passwordResetCodeStore.deleteCode(command.email());
			
			throw new MemberWithdrawnException(member.getEmail());
		}
		
		passwordResetCodeStore.deleteCode(command.email());

		String resetToken = generateToken();
		passwordResetTokenStore.saveToken(member.getEmail(), resetToken, TOKEN_EXPIRATION);
		
		return PasswordResetConfirmResponse.of(resetToken);
		
    }

	// 비밀번호 재설정
	@Transactional
	public void resetPassword(PasswordResetCommand command) {

		String email = passwordResetTokenStore.getEmail(command.resetToken());

		if (email == null) {

			throw new AuthCodeInvalidException("비밀번호 재설정 토큰이 만료되었거나 유효하지 않습니다.");
		}

		Member member = memberRepository.findByEmail(email)
				.orElseThrow(() -> new MemberNotFoundException(email));

		if (member.getStatus() == MemberStatus.WITHDRAWN) {

			passwordResetTokenStore.deleteToken(command.resetToken());
			throw new MemberWithdrawnException(member.getEmail());
		}

		member.changePassword(passwordEncoder.encode(command.newPassword()));
		passwordResetTokenStore.deleteToken(command.resetToken());
	}
	
	private String generateCode() {
		
		return String.format("%06d", secureRandom.nextInt(CODE_BOUND));
	}

	private String generateToken() {

		return UUID.randomUUID().toString().replace("-", "");
	}
}
