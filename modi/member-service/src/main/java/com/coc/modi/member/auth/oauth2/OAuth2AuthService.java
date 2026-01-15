package com.coc.modi.member.auth.oauth2;

import java.util.Optional;
import java.util.UUID;

import com.coc.modi.common.ErrorCode;
import com.coc.modi.member.auth.application.MemberAuthService;
import com.coc.modi.member.auth.application.dto.MemberLoginResponse;
import com.coc.modi.member.auth.application.dto.OAuth2ConnectCommand;
import com.coc.modi.member.auth.application.dto.OAuth2SignupCommand;
import com.coc.modi.member.auth.infrastructure.EmailVerificationTokenStore;
import com.coc.modi.member.outbox.MemberOutboxService;
import com.coc.modi.member.member.domain.Member;
import com.coc.modi.member.member.domain.MemberRepository;
import com.coc.modi.member.member.domain.MemberRole;
import com.coc.modi.member.member.exception.EmailDuplicatedException;
import com.coc.modi.member.member.exception.MemberException;
import com.coc.modi.member.member.exception.PhoneDuplicatedException;
import com.coc.modi.kafka.event.MemberCreatedEvent;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OAuth2AuthService {

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final EmailVerificationTokenStore emailVerificationTokenStore;
	private final OAuth2SignupTokenStore signupTokenStore;
	private final MemberAuthService memberAuthService;
	private final MemberOutboxService memberOutboxService;

	@Transactional(readOnly = true)
	public OAuth2LoginResult handleLogin(OAuth2MemberInfo memberInfo) {

		Optional<Member> linkedMember = memberRepository.findByProviderAndProviderId(
				memberInfo.provider().normalized(),
				memberInfo.providerId()
		);
		if (linkedMember.isPresent()) {
			return OAuth2LoginResult.login(linkedMember.get());
		}

		String token = signupTokenStore.issue(OAuth2SignupPayload.from(memberInfo));
		return OAuth2LoginResult.signupRequired(token);
	}

	@Transactional
	public MemberLoginResponse signup(OAuth2SignupCommand command, boolean secureCookie) {

		OAuth2SignupPayload payload = signupTokenStore.getPayload(command.signupToken());

		validateVerificationToken(command.email(), command.verificationToken());

		if (memberRepository.existsByEmail(command.email())) {
			throw new EmailDuplicatedException(command.email());
		}
		if (memberRepository.existsByPhone(command.phone())) {
			throw new PhoneDuplicatedException(command.phone());
		}
		if (memberRepository.findByProviderAndProviderId(payload.provider(), payload.providerId()).isPresent()) {
			throw new MemberException(ErrorCode.CONFLICT, "이미 연결된 소셜 계정입니다.");
		}

		String encodedPassword = passwordEncoder.encode(UUID.randomUUID().toString());
		String name = resolveName(payload, command.email());

		Member member = Member.create(command.email(), encodedPassword, name, command.phone(), MemberRole.MEMBER);
		member.connectProvider(payload.provider(), payload.providerId());
		Member saved = memberRepository.save(member);

		memberOutboxService.enqueueMemberCreated(MemberCreatedEvent.of(saved.getId(), saved.getEmail()));

		emailVerificationTokenStore.deleteToken(command.verificationToken());
		signupTokenStore.delete(command.signupToken());

		return memberAuthService.issueTokens(saved, secureCookie);
	}

	@Transactional
	public void connect(Long memberId, OAuth2ConnectCommand command) {

		OAuth2SignupPayload payload = signupTokenStore.getPayload(command.signupToken());

		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new MemberException(ErrorCode.MEMBER_NOT_FOUND));

		if (member.getProvider() != null && member.getProviderId() != null) {
			if (!member.getProvider().equals(payload.provider()) || !member.getProviderId().equals(payload.providerId())) {
				throw new MemberException(ErrorCode.CONFLICT, "이미 다른 소셜 계정이 연결되어 있습니다.");
			}
		}

		Optional<Member> existing = memberRepository.findByProviderAndProviderId(payload.provider(), payload.providerId());
		if (existing.isPresent() && !existing.get().getId().equals(memberId)) {
			throw new MemberException(ErrorCode.CONFLICT, "이미 연결된 소셜 계정입니다.");
		}

		member.connectProvider(payload.provider(), payload.providerId());
		signupTokenStore.delete(command.signupToken());
	}

	private void validateVerificationToken(String email, String verificationToken) {

		if (verificationToken == null || verificationToken.isBlank()) {
			throw new MemberException(ErrorCode.AUTH_CODE_INVALID, "이메일 인증 토큰이 필요합니다.");
		}

		String storedEmail = emailVerificationTokenStore.getEmail(verificationToken);
		if (storedEmail == null) {
			throw new MemberException(ErrorCode.AUTH_CODE_INVALID, "이메일 인증 토큰이 만료되었거나 유효하지 않습니다.");
		}
		if (!storedEmail.equals(email)) {
			throw new MemberException(ErrorCode.AUTH_CODE_INVALID, "이메일 인증 토큰이 이메일과 일치하지 않습니다.");
		}
	}

	private String resolveName(OAuth2SignupPayload payload, String email) {

		if (payload.name() != null && !payload.name().isBlank()) {
			return payload.name();
		}
		if (email != null && email.contains("@")) {
			return email.substring(0, email.indexOf('@'));
		}
		return "modi-member";
	}
}
