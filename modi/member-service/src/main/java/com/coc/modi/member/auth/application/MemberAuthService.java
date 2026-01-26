package com.coc.modi.member.auth.application;

import java.time.Duration;

import com.coc.modi.common.ErrorCode;
import com.coc.modi.member.auth.application.dto.LogoutResponse;
import com.coc.modi.member.auth.application.dto.MemberLoginCommand;
import com.coc.modi.member.auth.application.dto.MemberLoginResponse;
import com.coc.modi.member.auth.application.dto.TokenReissueResponse;
import com.coc.modi.member.member.domain.Member;
import com.coc.modi.member.member.domain.MemberRepository;
import com.coc.modi.member.member.domain.MemberStatus;
import com.coc.modi.member.member.exception.MemberAccessDeniedException;
import com.coc.modi.member.member.exception.MemberException;
import com.coc.modi.member.member.exception.MemberNotFoundException;
import com.coc.modi.member.member.exception.MemberPasswordMismatchException;
import com.coc.modi.member.security.JwtTokenProvider;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberAuthService {
	
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final RefreshTokenService refreshTokenService;
	private final RefreshCookieManager refreshCookieManager;
	
	// 로그인
	public MemberLoginResponse login(MemberLoginCommand command, boolean secureCookie) {
		
		Member member = memberRepository.findByEmail(command.email())
				.orElseThrow(() -> new MemberNotFoundException(command.email()));
		
		if (!passwordEncoder.matches(command.password(), member.getPassword())) {
			
			throw new MemberPasswordMismatchException("비밀번호가 일치하지 않습니다.");
		}
		
		if (member.getStatus() == MemberStatus.WITHDRAWN) {
			
			throw new MemberAccessDeniedException("탈퇴한 회원입니다.");
		}
		
		if (member.getStatus() == MemberStatus.INACTIVE) {
			
			throw new MemberAccessDeniedException("정지된 회원입니다.");
		}
		
		String accessToken = jwtTokenProvider.generateAccessToken(member.getId());
		String refreshToken = jwtTokenProvider.generateRefreshToken(member.getId());
		
		Duration ttl = Duration.ofMillis(jwtTokenProvider.getRefreshTokenValidityInMs());
		
		refreshTokenService.save(member.getId(), refreshToken, ttl);
		
		return new MemberLoginResponse(
				accessToken,
				MemberLoginResponse.MemberData.from(member),
				refreshCookieManager.create(refreshToken,  ttl, secureCookie)
		);
	}

	public MemberLoginResponse issueTokens(Member member, boolean secureCookie) {

		if (member.getStatus() == MemberStatus.WITHDRAWN) {
			throw new MemberAccessDeniedException("탈퇴한 회원입니다.");
		}
		if (member.getStatus() == MemberStatus.INACTIVE) {
			throw new MemberAccessDeniedException("정지된 회원입니다.");
		}

		String accessToken = jwtTokenProvider.generateAccessToken(member.getId());
		String refreshToken = jwtTokenProvider.generateRefreshToken(member.getId());

		Duration ttl = Duration.ofMillis(jwtTokenProvider.getRefreshTokenValidityInMs());

		refreshTokenService.save(member.getId(), refreshToken, ttl);

		return new MemberLoginResponse(
				accessToken,
				MemberLoginResponse.MemberData.from(member),
				refreshCookieManager.create(refreshToken, ttl, secureCookie)
		);
	}
	
	public TokenReissueResponse reissue(HttpServletRequest request, boolean secureCookie) {
		
		String refreshToken = refreshCookieManager.extract(request);
		
		if (refreshToken == null) {
			
			throw new MemberException(ErrorCode.UNAUTHORIZED); // Todo : 이후에 토큰 관련 Custom Exception 작성 필요
		}
		
		if (!jwtTokenProvider.validateToken(refreshToken)) {
			
			throw new MemberException(ErrorCode.UNAUTHORIZED);
		}
		
		Long memberId = jwtTokenProvider.getMemberId(refreshToken);
		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new MemberNotFoundException(memberId));

		if (member.getStatus() == MemberStatus.WITHDRAWN) {
			refreshTokenService.delete(memberId);
			throw new MemberAccessDeniedException("탈퇴한 회원입니다.");
		}
		if (member.getStatus() == MemberStatus.INACTIVE) {
			refreshTokenService.delete(memberId);
			throw new MemberAccessDeniedException("정지된 회원입니다.");
		}
		
		if (!refreshTokenService.matches(memberId, refreshToken)) {
			
			throw new MemberException(ErrorCode.UNAUTHORIZED);
		}
		
		String newAccessToken = jwtTokenProvider.generateAccessToken(memberId);
		String newRefreshToken = jwtTokenProvider.generateRefreshToken(memberId);
		
		Duration ttl = Duration.ofMillis(jwtTokenProvider.getRefreshTokenValidityInMs());
		
		refreshTokenService.save(memberId, newRefreshToken, ttl);
		
		return new TokenReissueResponse(
				newAccessToken,
				refreshCookieManager.create(newRefreshToken, ttl, secureCookie)
		);
	}
	
	public LogoutResponse logout(HttpServletRequest servletRequest, boolean secureCookie) {
		
		String refreshToken = refreshCookieManager.extract(servletRequest);
		
		if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
			
			Long memberId = jwtTokenProvider.getMemberId(refreshToken);
			
			if (refreshTokenService.matches(memberId, refreshToken)) {
				
				refreshTokenService.delete(memberId);
			}
		}
		
		return new LogoutResponse(refreshCookieManager.clear(secureCookie));
	}
}
