package com.coc.modi.member.member.application;

import com.coc.modi.common.ErrorCode;
import com.coc.modi.member.auth.infrastructure.EmailVerificationTokenStore;
import com.coc.modi.member.auth.application.RefreshTokenService;
import com.coc.modi.kafka.event.MemberCreatedEvent;
import com.coc.modi.kafka.event.MemberRoleChangedEvent;
import com.coc.modi.member.member.application.dto.CreateMemberCommand;
import com.coc.modi.member.member.application.dto.InternalAdminMemberCreateCommand;
import com.coc.modi.member.member.application.dto.InternalAdminMemberCreateResponse;
import com.coc.modi.member.member.application.dto.MemberEmailResponse;
import com.coc.modi.member.member.application.dto.MemberPageResponse;
import com.coc.modi.member.member.application.dto.MemberProfileResponse;
import com.coc.modi.member.member.application.dto.MemberSignupResponse;
import com.coc.modi.member.member.application.dto.MemberSummaryResponse;
import com.coc.modi.member.member.application.dto.UpdateMemberCommand;
import com.coc.modi.member.member.application.dto.UpdateMemberPasswordCommand;
import com.coc.modi.member.member.domain.Member;
import com.coc.modi.member.member.domain.MemberRole;
import com.coc.modi.member.member.domain.MemberRepository;
import com.coc.modi.member.member.exception.AuthCodeInvalidException;
import com.coc.modi.member.member.exception.EmailDuplicatedException;
import com.coc.modi.member.member.exception.MemberException;
import com.coc.modi.member.member.exception.MemberPasswordMismatchException;
import com.coc.modi.member.member.exception.MemberNotFoundException;
import com.coc.modi.member.member.exception.PhoneDuplicatedException;
import com.coc.modi.member.member.exception.WalletBalanceCheckFailedException;
import com.coc.modi.member.member.exception.WalletBalanceRemainingException;
import com.coc.modi.member.outbox.MemberOutboxService;
import com.coc.modi.member.member.infrastructure.client.AccountClientAdapter;
import com.coc.modi.member.member.infrastructure.client.dto.MemberWalletResponse;
import com.coc.modi.member.security.JwtTokenProvider;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
@Service
@Slf4j
@RequiredArgsConstructor
public class MemberService {

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final AccountClientAdapter accountClientAdapter;
	private final EmailVerificationTokenStore emailVerificationTokenStore;
	private final RefreshTokenService refreshTokenService;
	private final MemberOutboxService memberOutboxService;
	private final JwtTokenProvider jwtTokenProvider;
	
	// 회원가입
	@Transactional
	public MemberSignupResponse signup(CreateMemberCommand command) {
		
		validateVerificationToken(command.email(), command.verificationToken());

		// 중복 이메일인지 확인
		if (memberRepository.existsByEmail(command.email())) {
			
			throw new EmailDuplicatedException(command.email());
		}
		
		// 중복 휴대폰 번호인지 확인
		if (memberRepository.existsByPhone(command.phone())) {
			
			throw new PhoneDuplicatedException(command.phone());
		}
		
		String encodedPassword = passwordEncoder.encode(command.password());
		
		Member member = Member.create(
				command.email(),
				encodedPassword,
				command.name(),
				command.phone(),
				MemberRole.MEMBER
		);
		
		Member saved = memberRepository.save(member);
		
		memberOutboxService.enqueueMemberCreated(
				MemberCreatedEvent.of(saved.getId(), saved.getEmail())
		);

		emailVerificationTokenStore.deleteToken(command.verificationToken());
		
		return MemberSignupResponse.from(saved);
	}
	
	// 내 정보 조회
	@Transactional(readOnly = true)
	public MemberProfileResponse getProfile(Long memberId) {
		
		Member member = getMemberOrThrow(memberId);
		
		return MemberProfileResponse.from(member);
	}
	
	
	// 회원 정보 수정
	@Transactional
	public MemberProfileResponse updateProfile(UpdateMemberCommand command) {
		
		Member member = getMemberOrThrow(command.memberId());
		
		if (command.name() != null && !command.name().isBlank()) {
			
			member.changeName(command.name());
		}
		
		if (command.phone() != null && !command.phone().isBlank()) {
			if (!command.phone().equals(member.getPhone())
					&& memberRepository.existsByPhone(command.phone())) {
				throw new PhoneDuplicatedException(command.phone());
			}
			member.changePhone(command.phone());
		}
		
		return MemberProfileResponse.from(member);
	}
	
	// 비밀번호 수정
	@Transactional
	public void updatePassword(UpdateMemberPasswordCommand command) {
		
		Member member = getMemberOrThrow(command.memberId());

		if (!passwordEncoder.matches(command.currentPassword(), member.getPassword())) {
			throw new MemberPasswordMismatchException("기존 비밀번호가 일치하지 않습니다.");
		}

		String encodedPassword = passwordEncoder.encode(command.newPassword());
		
		member.changePassword(encodedPassword);
	}
	
	// 회원 탈퇴
	@Transactional
	public void deleteMember(Long memberId) {
		
		Member member = getMemberOrThrow(memberId);
		
		MemberWalletResponse wallet;
		
		try {
			
			// 지갑에 잔액 남아있는지 내부API 확인
			wallet = accountClientAdapter.getWalletBalance(memberId);
		} catch (FeignException ex) {
			
			log.error("Failed to fetch wallet balance for memberId={}", memberId, ex);
			
			throw new WalletBalanceCheckFailedException();
		}
		
		// 지갑에 잔액 남아있으면 예외처리
		if (wallet != null && wallet.balance() != null && wallet.balance().compareTo(BigDecimal.ZERO) > 0) {
			
			log.error("Wallet balance is not null. memberId={}, wallet={}", memberId, wallet);
			
			throw new WalletBalanceRemainingException();
		}
		
		member.withdraw();
		refreshTokenService.delete(memberId);
	}
	
	private Member getMemberOrThrow(Long memberId) {
		
		return memberRepository.findById(memberId)
				.orElseThrow(() -> new MemberNotFoundException(memberId));
	}
	
	private void validateVerificationToken(String email, String verificationToken) {

		if (verificationToken == null || verificationToken.isBlank()) {

			throw new AuthCodeInvalidException("이메일 인증 토큰이 필요합니다.");
		}

		String storedEmail = emailVerificationTokenStore.getEmail(verificationToken);

		if (storedEmail == null) {

			throw new AuthCodeInvalidException("이메일 인증 토큰이 만료되었거나 유효하지 않습니다.");
		}

		if (!storedEmail.equals(email)) {

			throw new AuthCodeInvalidException("이메일 인증 토큰이 이메일과 일치하지 않습니다.");
		}
	}
	
	
	@Transactional(readOnly = true)
	public MemberEmailResponse getMemberEmail(Long memberId) {
		
		Member member = getMemberOrThrow(memberId);
		
		return MemberEmailResponse.from(member);
	}

	@Transactional(readOnly = true)
	public MemberSummaryResponse getMemberSummary(Long memberId) {

		Member member = getMemberOrThrow(memberId);
		return MemberSummaryResponse.from(member);
	}

	@Transactional(readOnly = true)
	public MemberSummaryResponse getMemberSummaryByEmail(String email) {

		if (email == null || email.isBlank()) {
			throw new IllegalArgumentException("email은 필수입니다.");
		}

		Member member = memberRepository.findByEmail(email)
				.orElseThrow(() -> new MemberNotFoundException(email));
		return MemberSummaryResponse.from(member);
	}

	@Transactional(readOnly = true)
	public MemberPageResponse getMemberPage(Pageable pageable) {

		Page<Member> members = memberRepository.findAll(pageable);
		return MemberPageResponse.from(members);
	}

	@Transactional(readOnly = true)
	public List<MemberSummaryResponse> getMembersByIds(List<Long> memberIds) {

		if (memberIds == null || memberIds.isEmpty()) {
			return List.of();
		}

		List<Member> members = memberRepository.findByIdIn(memberIds);
		Map<Long, MemberSummaryResponse> summaries = members.stream()
				.map(MemberSummaryResponse::from)
				.collect(Collectors.toMap(MemberSummaryResponse::memberId, summary -> summary));

		return memberIds.stream()
				.map(summaries::get)
				.filter(Objects::nonNull)
				.toList();
	}

	@Transactional
	public InternalAdminMemberCreateResponse createAdmin(InternalAdminMemberCreateCommand command) {

		if (command == null) {
			throw new IllegalArgumentException("요청 본문이 비어 있습니다.");
		}
		if (command.email() == null || command.email().isBlank()) {
			throw new IllegalArgumentException("email은 필수입니다.");
		}
		if (command.password() == null || command.password().isBlank()) {
			throw new IllegalArgumentException("password는 필수입니다.");
		}
		if (command.name() == null || command.name().isBlank()) {
			throw new IllegalArgumentException("name은 필수입니다.");
		}
		if (command.phone() == null || command.phone().isBlank()) {
			throw new IllegalArgumentException("phone은 필수입니다.");
		}

		if (memberRepository.existsByEmail(command.email())) {
			throw new EmailDuplicatedException(command.email());
		}

		if (memberRepository.existsByPhone(command.phone())) {
			throw new PhoneDuplicatedException(command.phone());
		}

		String encodedPassword = passwordEncoder.encode(command.password());

		Member member = Member.create(
				command.email(),
				encodedPassword,
				command.name(),
				command.phone(),
				MemberRole.ADMIN
		);

		Member saved = memberRepository.save(member);

		log.info("admin_account_created createdBy={} targetMemberId={}", command.createdBy(), saved.getId());

		return InternalAdminMemberCreateResponse.from(saved);
	}
	
	
	@Transactional
	public String updateRoleToSeller(Long memberId) {
		
		Member member = getMemberOrThrow(memberId);
		
		if (member.getRole() == MemberRole.SELLER) {
			
			throw new MemberException(ErrorCode.MEMBER_ROLE_INVALID);
		}
		
		member.updateRole(MemberRole.SELLER);

		memberOutboxService.enqueueMemberRoleChanged(
				MemberRoleChangedEvent.of(memberId, member.getRole().name())
		);
		
		return jwtTokenProvider.generateAccessToken(memberId);
	}

	@Transactional(readOnly = true)
	public List<String> getMemberRoles(Long memberId) {

		Member member = getMemberOrThrow(memberId);
		return rolesFor(member.getRole());
	}

	private List<String> rolesFor(MemberRole role) {

		if (role == MemberRole.ADMIN) {
			return List.of(MemberRole.MEMBER.name(), MemberRole.ADMIN.name());
		}
		if (role == MemberRole.SELLER) {
			return List.of(MemberRole.MEMBER.name(), MemberRole.SELLER.name());
		}
		return List.of(MemberRole.MEMBER.name());
	}
}
