package com.coc.modi.member.auth.application;

import com.coc.modi.member.auth.application.dto.PasswordResetConfirmCommand;
import com.coc.modi.member.auth.application.dto.PasswordResetSendCommand;
import com.coc.modi.member.auth.infrastructure.PasswordResetCodeStore;
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

@Service
@RequiredArgsConstructor
public class PasswordResetService {
	
	private static final int CODE_BOUND = 1_000_000;
	private static final Duration EXPIRATION = Duration.ofMinutes(5);
	
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final PasswordResetCodeStore passwordResetCodeStore;
	private final EmailSender emailSender;
	private final SecureRandom secureRandom = new SecureRandom();
	
    @Transactional
    public void sendResetCode(PasswordResetSendCommand command) {
		
		Member member = memberRepository.findByEmail(command.email())
				.orElseThrow(() -> new MemberNotFoundException(command.email()));
		
		String code = generateCode();
		
		passwordResetCodeStore.saveCode(member.getEmail(), code, EXPIRATION);
		emailSender.sendVerificationCode(member.getEmail(), code);
		
    }

    // 비밀번호 재설정
    @Transactional
    public void resetPassword(PasswordResetConfirmCommand command) {
		
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
		
		member.changePassword(passwordEncoder.encode(command.newPassword()));
		
		passwordResetCodeStore.deleteCode(command.email());
		
    }
	
	private String generateCode() {
		
		return String.format("%06d", secureRandom.nextInt(CODE_BOUND));
	}
}
