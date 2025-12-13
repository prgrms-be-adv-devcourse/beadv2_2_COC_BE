package com.coc.modi.member.member.application;

import com.coc.modi.member.auth.infrastructure.EmailVerificationCodeStore;
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
import com.coc.modi.member.member.exception.MemberNotFoundException;
import com.coc.modi.member.member.exception.PasswordMismatchException;
import com.coc.modi.member.member.exception.WalletCreationFailedException;
import com.coc.modi.member.member.infrastructure.client.AccountFeignClient;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {
	
	private static final Pattern VERIFICATION_CODE_PATTERN = Pattern.compile("^\\d{6}$");
	
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final AccountFeignClient accountFeignClient;
	private final MemberValidationService memberValidationService;
	private final EmailVerificationCodeStore emailVerificationCodeStore;
	
	// 회원가입
	@Transactional
	public MemberSignupResponse signup(CreateMemberCommand command) {
		
		memberValidationService.validateEmail(command.email());
		
		if (memberRepository.existsByEmail(command.email())) {
			
			throw new EmailDuplicatedException(command.email());
		}
		
		memberValidationService.validatePassword(command.password());
		
		memberValidationService.validatePhone(command.phone());
		
		String encodedPassword = passwordEncoder.encode(command.password());
		
		Member member = Member.create(
				command.email(),
				encodedPassword,
				command.name(),
				command.phone(),
				MemberRole.USER
		);
		
		Member saved = memberRepository.save(member);
		
		// 회원 지갑 생성 요청
		try {
			
			accountFeignClient.createWallet(saved.getId());
		} catch (FeignException ex) {
			
			log.error("Failed to create wallet for memberId={}", saved.getId(), ex);
			
			throw new WalletCreationFailedException();
		}
		
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
	public MemberProfileResponse updateProfile(Long memberId,
											   UpdateMemberCommand command) {
		
		Member member = getMemberOrThrow(memberId);
		
		if (command.name() != null && !command.name().isBlank()) {
			
			member.changeName(command.name());
		}
		
		if (command.phone() != null && !command.phone().isBlank()) {
			
			memberValidationService.validatePhone(command.phone());
			
			member.changePhone(command.phone());
		}
		
		return MemberProfileResponse.from(member);
	}
	
	// 비밀번호 수정
	@Transactional
	public void updatePassword(Long authenticatedMemberId,
							   Long memberId,
							   UpdateMemberPasswordCommand command) {
		
		if (!authenticatedMemberId.equals(memberId)) {
			
			throw new PasswordMismatchException("본인만 비밀번호를 변경할 수 있습니다.");
		}
		
		Member member = getMemberOrThrow(memberId);
		
		// 이메일 유효성 검사
		memberValidationService.validateEmail(command.email());
		
		if (!member.getEmail().equals(command.email())) {
			
			throw new PasswordMismatchException("이메일이 일치하지 않습니다.");
		}
		
		if (command.name() == null || command.name().isBlank()) {
			
			throw new PasswordMismatchException("이름을 입력해주세요.");
		}
		
		if (!member.getName().equals(command.name())) {
			
			throw new PasswordMismatchException("이름이 일치하지 않습니다.");
		}
		
		validateVerificationCode(command.email(), command.verificationCode());
		
		memberValidationService.validatePassword(command.password());
		
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
	
}
