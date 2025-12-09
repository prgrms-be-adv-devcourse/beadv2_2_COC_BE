package com.coc.modi.auth.domain;

import java.util.Optional;

public interface EmailVerificationRepository {
	
	Optional<EmailVerification> findByEmail(String email);
	
	EmailVerification save(EmailVerification emailVerification);
}
