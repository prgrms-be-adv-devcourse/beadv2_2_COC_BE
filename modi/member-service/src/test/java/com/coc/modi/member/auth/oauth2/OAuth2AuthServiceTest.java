package com.coc.modi.member.auth.oauth2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.coc.modi.member.auth.application.MemberAuthService;
import com.coc.modi.member.auth.application.dto.MemberLoginResponse;
import com.coc.modi.member.auth.application.dto.OAuth2ConnectCommand;
import com.coc.modi.member.auth.application.dto.OAuth2SignupCommand;
import com.coc.modi.member.auth.infrastructure.EmailVerificationTokenStore;
import com.coc.modi.member.member.domain.Member;
import com.coc.modi.member.member.domain.MemberRepository;
import com.coc.modi.member.member.domain.MemberRole;
import com.coc.modi.member.member.exception.MemberException;
import com.coc.modi.member.outbox.MemberOutboxService;
import com.coc.modi.kafka.event.MemberCreatedEvent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OAuth2AuthServiceTest {

	@Mock
	private MemberRepository memberRepository;
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private EmailVerificationTokenStore emailVerificationTokenStore;
	@Mock
	private OAuth2SignupTokenStore signupTokenStore;
	@Mock
	private MemberAuthService memberAuthService;
	@Mock
	private MemberOutboxService memberOutboxService;

	@InjectMocks
	private OAuth2AuthService oauth2AuthService;

	@Test
	void handle_login_returns_member_when_provider_linked() {

		Member member = Member.create("linked@example.com", "pw", "Linked", "01012345678", MemberRole.MEMBER);
		when(memberRepository.findByProviderAndProviderId("kakao", "kakao-1"))
				.thenReturn(Optional.of(member));

		OAuth2MemberInfo info = new OAuth2MemberInfo(OAuth2Provider.KAKAO, "kakao-1", null, "Kakao User");

		OAuth2LoginResult result = oauth2AuthService.handleLogin(info);

		assertThat(result.isLogin()).isTrue();
		assertThat(result.member()).isSameAs(member);
		verify(signupTokenStore, never()).issue(any());
	}

	@Test
	void handle_login_returns_signup_required_when_not_linked() {

		when(memberRepository.findByProviderAndProviderId("naver", "naver-1"))
				.thenReturn(Optional.empty());
		when(signupTokenStore.issue(any())).thenReturn("signup-token");

		OAuth2MemberInfo info = new OAuth2MemberInfo(OAuth2Provider.NAVER, "naver-1", null, "Naver User");

		OAuth2LoginResult result = oauth2AuthService.handleLogin(info);

		assertThat(result.isLogin()).isFalse();
		assertThat(result.signupToken()).isEqualTo("signup-token");
	}

	@Test
	void signup_creates_member_and_issues_tokens() {

		OAuth2SignupPayload payload = new OAuth2SignupPayload("kakao", "kakao-2", "Kakao User", null);
		when(signupTokenStore.getPayload("signup-token")).thenReturn(payload);
		when(emailVerificationTokenStore.getEmail("verify-token")).thenReturn("user@example.com");
		when(memberRepository.existsByEmail("user@example.com")).thenReturn(false);
		when(memberRepository.existsByPhone("01012345678")).thenReturn(false);
		when(memberRepository.findByProviderAndProviderId("kakao", "kakao-2"))
				.thenReturn(Optional.empty());
		when(passwordEncoder.encode(any())).thenReturn("encoded");

		Member saved = Member.create("user@example.com", "encoded", "Kakao User", "01012345678", MemberRole.MEMBER);
		saved.connectProvider("kakao", "kakao-2");
		ReflectionTestUtils.setField(saved, "id", 1L);
		when(memberRepository.save(any(Member.class))).thenReturn(saved);

		MemberLoginResponse response = new MemberLoginResponse(
				"access",
				MemberLoginResponse.MemberData.from(saved),
				ResponseCookie.from("refresh_token", "refresh").build()
		);
		when(memberAuthService.issueTokens(eq(saved), eq(true))).thenReturn(response);

		OAuth2SignupCommand command = new OAuth2SignupCommand(
				"signup-token",
				"user@example.com",
				"01012345678",
				"verify-token"
		);

		MemberLoginResponse result = oauth2AuthService.signup(command, true);

		assertThat(result).isSameAs(response);

		ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
		verify(memberRepository).save(memberCaptor.capture());
		Member created = memberCaptor.getValue();
		assertThat(created.getProvider()).isEqualTo("kakao");
		assertThat(created.getProviderId()).isEqualTo("kakao-2");

		ArgumentCaptor<MemberCreatedEvent> eventCaptor = ArgumentCaptor.forClass(MemberCreatedEvent.class);
		verify(memberOutboxService).enqueueMemberCreated(eventCaptor.capture());
		assertThat(eventCaptor.getValue().memberId()).isEqualTo(1L);
		assertThat(eventCaptor.getValue().email()).isEqualTo("user@example.com");

		verify(emailVerificationTokenStore).deleteToken("verify-token");
		verify(signupTokenStore).delete("signup-token");
	}

	@Test
	void connect_rejects_when_member_already_connected_to_different_provider() {

		Member member = Member.create("user@example.com", "pw", "User", "01012345678", MemberRole.MEMBER);
		member.connectProvider("google", "google-1");
		when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
		when(signupTokenStore.getPayload("signup-token"))
				.thenReturn(new OAuth2SignupPayload("kakao", "kakao-2", "Kakao User", null));

		assertThatThrownBy(() -> oauth2AuthService.connect(1L, new OAuth2ConnectCommand("signup-token")))
				.isInstanceOf(MemberException.class);
	}
}
