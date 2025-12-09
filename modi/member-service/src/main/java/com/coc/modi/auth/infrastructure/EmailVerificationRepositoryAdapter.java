package com.coc.modi.auth.infrastructure;

import com.coc.modi.auth.domain.EmailVerification;
import com.coc.modi.auth.domain.EmailVerificationRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EmailVerificationRepositoryAdapter implements EmailVerificationRepository {
	
	private final EmailVerificationJpaRepository emailVerificationJpaRepository;
	
	@Override
	public Optional<EmailVerification> findByEmail(String email) {
		
		return emailVerificationJpaRepository.findByEmail(email);
	}
	
	@Override
	public EmailVerification save(EmailVerification emailVerification) {
		
		return emailVerificationJpaRepository.save(emailVerification);
	}
}
