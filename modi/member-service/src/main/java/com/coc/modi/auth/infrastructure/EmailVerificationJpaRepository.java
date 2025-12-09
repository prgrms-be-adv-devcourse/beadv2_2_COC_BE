package com.coc.modi.auth.infrastructure;

import com.coc.modi.auth.domain.EmailVerification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationJpaRepository extends JpaRepository<EmailVerification, Long> {
	
	Optional<EmailVerification> findByEmail(String email);
}
