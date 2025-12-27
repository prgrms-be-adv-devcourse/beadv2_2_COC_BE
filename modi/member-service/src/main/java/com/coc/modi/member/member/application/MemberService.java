package com.coc.modi.member.member.application;

import com.coc.modi.common.ErrorCode;
import com.coc.modi.member.auth.application.EmailVerificationService;
import com.coc.modi.member.auth.application.dto.SendEmailVerificationCommand;
import com.coc.modi.member.auth.infrastructure.EmailVerificationCodeStore;
import com.coc.modi.member.auth.infrastructure.EmailVerificationTokenStore;
import com.coc.modi.member.member.application.dto.CreateMemberCommand;
import com.coc.modi.member.member.application.dto.MemberProfileResponse;
import com.coc.modi.member.member.application.dto.MemberSignupResponse;
import com.coc.modi.member.member.application.dto.UpdateMemberCommand;
import com.coc.modi.member.member.application.dto.UpdateMemberPasswordCommand;
import com.coc.modi.member.member.domain.Member;
import com.coc.modi.member.member.domain.MemberRole;
import com.coc.modi.member.member.domain.MemberRepository;
import com.coc.modi.member.member.exception.AuthCodeInvalidException;
import com.coc.modi.member.member.exception.EmailDuplicatedException;
import com.coc.modi.member.member.exception.MemberEmailMismatchException;
import com.coc.modi.member.member.exception.MemberException;
import com.coc.modi.member.member.exception.MemberNameMismatchException;
import com.coc.modi.member.member.exception.MemberNotFoundException;
import com.coc.modi.member.member.exception.PhoneDuplicatedException;
import com.coc.modi.member.member.exception.WalletCreationFailedException;
import com.coc.modi.member.member.infrastructure.client.AccountClientAdapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {
	
	private static final Pattern VERIFICATION_CODE_PATTERN = Pattern.compile("^\\d{6}$");
	
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final AccountClientAdapter accountClientAdapter;
	private final EmailVerificationCodeStore emailVerificationCodeStore;
	private final EmailVerificationTokenStore emailVerificationTokenStore;
	private final EmailVerificationService emailVerificationService;
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
		
		// 회원 지갑 생성 요청
		try {
			
			accountClientAdapter.createWallet(saved.getId());
		} catch (Exception ex) {
			
			log.error("Failed to create wallet for memberId={}", saved.getId(), ex);
			
			throw new WalletCreationFailedException();
		}

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
			
			member.changePhone(command.phone());
		}
		
		return MemberProfileResponse.from(member);
	}
	
	// 비밀번호 수정
	@Transactional
	public void updatePassword(UpdateMemberPasswordCommand command) {
		
		Member member = getMemberOrThrow(command.memberId());
		
		if (!member.getEmail().equals(command.email())) {
			
			throw new MemberEmailMismatchException("이메일이 일치하지 않습니다.");
		}
		
		if (command.name() == null || command.name().isBlank()) {
			
			throw new MemberNameMismatchException("이름을 입력해주세요.");
		}
		
		if (!member.getName().equals(command.name())) {
			
			throw new MemberNameMismatchException("이름이 일치하지 않습니다.");
		}
		
		// 이메일 검증 코드 발송
		emailVerificationService.sendVerificationEmail(new SendEmailVerificationCommand(member.getEmail()));
		
		validateVerificationCode(command.email(), command.verificationCode());
		
		String encodedPassword = passwordEncoder.encode(command.password());
		
		member.changePassword(encodedPassword);
	}
	
	// 회원 탈퇴
	@Transactional
	public void deleteMember(Long memberId) {
		
		Member member = getMemberOrThrow(memberId);
		
		member.withdraw();
	}
	
	private Member getMemberOrThrow(Long memberId) {
		
		return memberRepository.findById(memberId)
				.orElseThrow(() -> new MemberNotFoundException(memberId));
	}
	
	private void validateVerificationCode(String email, String verificationCode) {
		
		if (verificationCode == null || verificationCode.isBlank()) {
			
			throw new AuthCodeInvalidException("인증 코드를 입력해주세요.");
		}
		
		if (!VERIFICATION_CODE_PATTERN.matcher(verificationCode).matches()) {
			
			throw new AuthCodeInvalidException("인증 코드는 6자리 숫자입니다.");
		}
		
		String storedCode = emailVerificationCodeStore.getCode(email);
		
		if (storedCode == null) {
			
			throw new AuthCodeInvalidException("이메일 인증 요청이 존재하지 않습니다.");
		}
		
		if (!storedCode.equals(verificationCode)) {
			
			throw new AuthCodeInvalidException("인증 코드가 일치하지 않습니다.");
		}
		
		emailVerificationCodeStore.deleteCode(email);
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
	
	@Transactional
	public String updateRoleToSeller(Long memberId) {
		
		Member member = getMemberOrThrow(memberId);
		
		if (member.getRole() == MemberRole.SELLER) {
			
			throw new MemberException(ErrorCode.MEMBER_ROLE_INVALID);
		}
		
		member.updateRole(MemberRole.SELLER);
		
		return jwtTokenProvider.generateAccessToken(memberId, member.getRole().name(), member.getName(), member.getEmail());
	}
}
