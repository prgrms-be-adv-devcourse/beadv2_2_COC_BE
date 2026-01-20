package com.coc.modi.member.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;

import com.coc.modi.member.auth.application.dto.LogoutResponse;
import com.coc.modi.member.auth.application.dto.TokenReissueResponse;
import com.coc.modi.member.member.exception.MemberException;
import com.coc.modi.member.security.JwtTokenProvider;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;

@ExtendWith(MockitoExtension.class)
class MemberAuthServiceTest {
	
	@Mock
	private JwtTokenProvider jwtTokenProvider;
	@Mock
	private RefreshTokenService refreshTokenService;
	@Mock
	private RefreshCookieManager refreshCookieManager;
	
	@InjectMocks
	private MemberAuthService memberAuthService;
	
	@Test
	void reissue_rotates_refresh_token_when_token_matches_store() {
		
		// 저장된 refresh 토큰과 일치하면 재발급하며 새 토큰으로 회전한다.
		HttpServletRequest request = mock(HttpServletRequest.class);
		String oldRefresh = "old-refresh";
		String newRefresh = "new-refresh";
		String newAccess = "new-access";
		
		when(refreshCookieManager.extract(request)).thenReturn(oldRefresh);
		when(jwtTokenProvider.validateToken(oldRefresh)).thenReturn(true);
		when(jwtTokenProvider.getMemberId(oldRefresh)).thenReturn(1L);
		when(refreshTokenService.matches(1L, oldRefresh)).thenReturn(true);
		when(jwtTokenProvider.generateAccessToken(1L)).thenReturn(newAccess);
		when(jwtTokenProvider.generateRefreshToken(1L)).thenReturn(newRefresh);
		when(jwtTokenProvider.getRefreshTokenValidityInMs()).thenReturn(3_600_000L);
		
		ResponseCookie rotatedCookie = ResponseCookie.from("refresh_token", newRefresh).build();
		when(refreshCookieManager.create(eq(newRefresh), eq(Duration.ofMillis(3_600_000L)), anyBoolean()))
				.thenReturn(rotatedCookie);
		
		TokenReissueResponse response = memberAuthService.reissue(request, false);
		
		assertThat(response.accessToken()).isEqualTo(newAccess);
		assertThat(response.responseCookie()).isSameAs(rotatedCookie);
		verify(refreshTokenService).save(1L, newRefresh, Duration.ofMillis(3_600_000L));
	}
	
	@Test
	void reissue_rejects_when_refresh_token_not_in_store() {
		
		// Redis에 없는 refresh 토큰이면 재발급을 거부한다.
		HttpServletRequest request = mock(HttpServletRequest.class);
		String stolenRefresh = "stolen-refresh";
		
		when(refreshCookieManager.extract(request)).thenReturn(stolenRefresh);
		when(jwtTokenProvider.validateToken(stolenRefresh)).thenReturn(true);
		when(jwtTokenProvider.getMemberId(stolenRefresh)).thenReturn(2L);
		when(refreshTokenService.matches(2L, stolenRefresh)).thenReturn(false);
		
		assertThatThrownBy(() -> memberAuthService.reissue(request, false))
				.isInstanceOf(MemberException.class);
		
		verify(refreshTokenService, never()).save(anyLong(), any(), any());
	}
	
	@Test
	void logout_deletes_refresh_token_when_matches_store() {
		
		// 로그아웃 시 저장된 토큰과 일치하면 Redis에서 삭제한다.
		HttpServletRequest request = mock(HttpServletRequest.class);
		String refreshToken = "valid-refresh";
		ResponseCookie cleared = ResponseCookie.from("refresh_token", "").build();
		
		when(refreshCookieManager.extract(request)).thenReturn(refreshToken);
		when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);
		when(jwtTokenProvider.getMemberId(refreshToken)).thenReturn(3L);
		when(refreshTokenService.matches(3L, refreshToken)).thenReturn(true);
		when(refreshCookieManager.clear(false)).thenReturn(cleared);
		
		LogoutResponse response = memberAuthService.logout(request, false);
		
		assertThat(response.clear()).isSameAs(cleared);
		verify(refreshTokenService).delete(3L);
	}
	
	@Test
	void logout_does_not_delete_when_refresh_token_is_not_saved() {
		
		// 저장되지 않은 토큰이면 삭제를 시도하지 않는다.
		HttpServletRequest request = mock(HttpServletRequest.class);
		String refreshToken = "unknown-refresh";
		ResponseCookie cleared = ResponseCookie.from("refresh_token", "").build();
		
		when(refreshCookieManager.extract(request)).thenReturn(refreshToken);
		when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);
		when(jwtTokenProvider.getMemberId(refreshToken)).thenReturn(4L);
		when(refreshTokenService.matches(4L, refreshToken)).thenReturn(false);
		when(refreshCookieManager.clear(false)).thenReturn(cleared);
		
		LogoutResponse response = memberAuthService.logout(request, false);
		
		assertThat(response.clear()).isSameAs(cleared);
		verify(refreshTokenService, never()).delete(anyLong());
	}
}
