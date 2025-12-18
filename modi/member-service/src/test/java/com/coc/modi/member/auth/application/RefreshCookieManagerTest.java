package com.coc.modi.member.auth.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;

	class RefreshCookieManagerTest {
	
	@Test
	void create_and_clear_share_same_cookie_attributes() {
		
		// 생성/삭제된 쿠키가 동일한 속성(path/domain/samesite/httpOnly/secure)을 유지하는지 확인한다.
		RefreshCookieManager manager = new RefreshCookieManager(
				"/member-service/api/auth",
				"example.com",
				"Lax"
		);
		
		Duration ttl = Duration.ofMinutes(30);
		
		ResponseCookie created = manager.create("token", ttl, true);
		ResponseCookie cleared = manager.clear(true);
		
		assertThat(created.getPath()).isEqualTo("/member-service/api/auth");
		assertThat(cleared.getPath()).isEqualTo(created.getPath());
		assertThat(created.getDomain()).isEqualTo("example.com");
		assertThat(cleared.getDomain()).isEqualTo("example.com");
		assertThat(created.getSameSite()).isEqualTo("Lax");
		assertThat(cleared.getSameSite()).isEqualTo("Lax");
		assertThat(created.isHttpOnly()).isTrue();
		assertThat(cleared.isHttpOnly()).isTrue();
		assertThat(created.isSecure()).isTrue();
		assertThat(cleared.isSecure()).isTrue();
		assertThat(created.getMaxAge()).isEqualTo(ttl);
		assertThat(cleared.getMaxAge()).isZero();
	}
}
