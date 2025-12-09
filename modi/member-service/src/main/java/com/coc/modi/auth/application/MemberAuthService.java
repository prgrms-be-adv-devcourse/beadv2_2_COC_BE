package com.coc.modi.auth.application;

import com.coc.modi.auth.application.dto.MemberLoginCommand;
import com.coc.modi.auth.application.dto.MemberLoginResponse;
import com.coc.modi.common.auth.JwtTokenProvider;
import com.coc.modi.member.domain.Member;
import com.coc.modi.member.domain.MemberRepository;

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
		
		// TODO : 나중에 커스텀 예외로 변경
		Member member = memberRepository.findByEmail(command.email())
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
		
		if (!passwordEncoder.matches(command.password(), member.getPassword())) {
			throw new IllegalArgumentException("비밀번호 불일치입니다.");
		}
		
		String accessToken = jwtTokenProvider.generateAccessToken(member.getId(), member.getRole().name());
		String refreshToken = jwtTokenProvider.generateRefreshToken(member.getId(), member.getRole().name());
		
		return MemberLoginResponse.of(accessToken, refreshToken, member);
	}
}
