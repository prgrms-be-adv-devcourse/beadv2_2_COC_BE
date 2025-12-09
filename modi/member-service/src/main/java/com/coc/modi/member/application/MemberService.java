package com.coc.modi.member.application;

import com.coc.modi.member.application.dto.CreateMemberCommand;
import com.coc.modi.member.application.dto.MemberProfileResponse;
import com.coc.modi.member.application.dto.MemberSignupResponse;
import com.coc.modi.member.domain.Member;
import com.coc.modi.member.domain.MemberRole;
import com.coc.modi.member.domain.MemberRepository;
import com.coc.modi.member.application.dto.UpdateMemberCommand;
import com.coc.modi.member.application.dto.UpdateMemberPasswordCommand;
import com.coc.modi.member.infrastructure.client.AccountFeignClient;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {
	
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final AccountFeignClient accountFeignClient;
	private final MemberValidationService memberValidationService;
	
	// 회원가입
	@Transactional
	public MemberSignupResponse signup(CreateMemberCommand command) {
		
		memberValidationService.validateEmail(command.email());
		
		if (memberRepository.existsByEmail(command.email())) {
			
			throw new IllegalArgumentException("Email already exists");
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
		accountFeignClient.createWallet(saved.getId());
		
		return MemberSignupResponse.from(saved);
	}
	
	// 내 정보 조회
	@Transactional(readOnly = true)
	public MemberProfileResponse getProfile(Authentication authentication) {
		
		Long memberId = (Long)authentication.getPrincipal();
		
		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new IllegalArgumentException("회원이 없습니다."));
		
		return MemberProfileResponse.from(member);
	}
	
	// 회원 정보 수정
	@Transactional
	public MemberProfileResponse updateProfile(Authentication authentication,
											   UpdateMemberCommand command) {
		
		Long memberId = (Long)authentication.getPrincipal();
		
		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new IllegalArgumentException("회원이 없습니다."));
		
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
	public void updatePassword(Authentication authentication,
							   Long memberId,
							   UpdateMemberPasswordCommand command) {
		
		Long authenticatedMemberId = (Long)authentication.getPrincipal();
		
		if (!authenticatedMemberId.equals(memberId)) {
			
			throw new IllegalArgumentException("본인만 비밀번호를 변경할 수 있습니다.");
		}
		
		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new IllegalArgumentException("회원이 없습니다."));
		
		if (command.name() == null || command.name().isBlank()) {
			
			throw new IllegalArgumentException("이름을 입력해주세요.");
		}
		
		if (!member.getName().equals(command.name())) {
			
			throw new IllegalArgumentException("이름이 일치하지 않습니다.");
		}
		
		memberValidationService.validatePassword(command.password());
		String encodedPassword = passwordEncoder.encode(command.password());
		member.changePassword(encodedPassword);
	}
	
	// 회원 탈퇴
	@Transactional
	public void deleteMember(Authentication authentication) {
		
		Long memberId = (Long)authentication.getPrincipal();
		
		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new IllegalArgumentException("회원이 없습니다."));
		
		member.withdraw();
	}
	
}
