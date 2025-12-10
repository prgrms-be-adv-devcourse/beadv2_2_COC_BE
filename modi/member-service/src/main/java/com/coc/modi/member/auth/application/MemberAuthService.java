package com.coc.modi.member.auth.application;

import com.coc.modi.member.auth.application.dto.MemberLoginCommand;
import com.coc.modi.member.auth.application.dto.MemberLoginResponse;
import com.coc.modi.common.auth.JwtTokenProvider;
import com.coc.modi.member.member.domain.Member;
import com.coc.modi.member.member.domain.MemberRepository;
import com.coc.modi.member.member.exception.MemberNotFoundException;
import com.coc.modi.member.member.exception.PasswordMismatchException;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberAuthService {
	
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	
	// 로그인
	public MemberLoginResponse login(MemberLoginCommand command) {
		
		Member member = memberRepository.findByEmail(command.email())
				.orElseThrow(() -> new MemberNotFoundException(command.email()));
		
		if (!passwordEncoder.matches(command.password(), member.getPassword())) {
			
			throw new PasswordMismatchException("비밀번호가 일치하지 않습니다.");
		}
		
		String accessToken = jwtTokenProvider.generateAccessToken(member.getId(), member.getRole().name());
		String refreshToken = jwtTokenProvider.generateRefreshToken(member.getId(), member.getRole().name());
		
		return MemberLoginResponse.of(accessToken, refreshToken, member);
	}
}
