package com.coc.modi.member.admin.blacklist.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coc.modi.admin.blacklist.presentation.AdminBlacklistController;
import com.coc.modi.common.ApiResponse;
import com.coc.modi.common.auth.CustomMember;
import com.coc.modi.admin.blacklist.application.BlacklistService;
import com.coc.modi.admin.blacklist.application.dto.BlacklistDetailResponse;
import com.coc.modi.admin.blacklist.application.dto.BlacklistReleaseCommand;
import com.coc.modi.admin.blacklist.application.dto.BlacklistSummaryResponse;
import com.coc.modi.admin.blacklist.application.dto.BlacklistSuspendCommand;
import com.coc.modi.admin.blacklist.domain.BlacklistStatus;
import com.coc.modi.admin.blacklist.presentation.dto.BlacklistReleaseRequest;
import com.coc.modi.admin.blacklist.presentation.dto.BlacklistSuspendRequest;
import com.coc.modi.admin.exception.AdminAccessDeniedException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class AdminBlacklistControllerTest {

	@Mock
	private BlacklistService blacklistService;

	@InjectMocks
	private AdminBlacklistController adminBlacklistController;

	@Test
	void getBlacklists_requiresAdmin() {
		assertThatThrownBy(() -> adminBlacklistController.getBlacklists(sellerMember(), null, Pageable.unpaged()))
				.isInstanceOf(AdminAccessDeniedException.class);
	}

	@Test
	void getBlacklists_returnsPage() {
		Page<BlacklistSummaryResponse> page = new PageImpl<>(List.of(summaryFixture()));
		when(blacklistService.getBlacklists(any(), any())).thenReturn(page);

		ResponseEntity<ApiResponse<Page<BlacklistSummaryResponse>>> response =
				adminBlacklistController.getBlacklists(adminMember(), null, Pageable.unpaged());

		assertThat(response.getBody().success()).isTrue();
		assertThat(response.getBody().data().getContent()).hasSize(1);
	}

	@Test
	void searchByEmail_requiresAdmin() {
		assertThatThrownBy(() -> adminBlacklistController.searchByEmail(sellerMember(), "test@test.com"))
				.isInstanceOf(AdminAccessDeniedException.class);
	}

	@Test
	void searchByEmail_returnsResponse() {
		when(blacklistService.searchByEmail(any())).thenReturn(summaryFixture());

		ResponseEntity<ApiResponse<BlacklistSummaryResponse>> response =
				adminBlacklistController.searchByEmail(adminMember(), "test@test.com");

		assertThat(response.getBody().success()).isTrue();
		assertThat(response.getBody().data().email()).isEqualTo("test@test.com");
	}

	@Test
	void getBlacklistDetail_requiresAdmin() {
		assertThatThrownBy(() -> adminBlacklistController.getBlacklistDetail(sellerMember(), 1L))
				.isInstanceOf(AdminAccessDeniedException.class);
	}

	@Test
	void getBlacklistDetail_returnsResponse() {
		when(blacklistService.getBlacklistDetail(any())).thenReturn(detailFixture());

		ResponseEntity<ApiResponse<BlacklistDetailResponse>> response =
				adminBlacklistController.getBlacklistDetail(adminMember(), 1L);

		assertThat(response.getBody().success()).isTrue();
		assertThat(response.getBody().data().memberId()).isEqualTo(1L);
	}

	@Test
	void suspend_requiresAdmin() {
		BlacklistSuspendRequest request = new BlacklistSuspendRequest(1L, "사유", "메모");
		assertThatThrownBy(() -> adminBlacklistController.suspend(sellerMember(), request))
				.isInstanceOf(AdminAccessDeniedException.class);
	}

	@Test
	void suspend_callsService() {
		BlacklistSuspendRequest request = new BlacklistSuspendRequest(1L, "사유", "메모");
		when(blacklistService.suspend(any(BlacklistSuspendCommand.class))).thenReturn(detailFixture());

		ResponseEntity<ApiResponse<BlacklistDetailResponse>> response =
				adminBlacklistController.suspend(adminMember(), request);

		assertThat(response.getStatusCode().value()).isEqualTo(201);
		ArgumentCaptor<BlacklistSuspendCommand> captor = ArgumentCaptor.forClass(BlacklistSuspendCommand.class);
		verify(blacklistService).suspend(captor.capture());
		assertThat(captor.getValue().createdBy()).isEqualTo(10L);
	}

	@Test
	void release_requiresAdmin() {
		assertThatThrownBy(() -> adminBlacklistController.release(sellerMember(), 1L, null))
				.isInstanceOf(AdminAccessDeniedException.class);
	}

	@Test
	void release_callsService() {
		BlacklistReleaseRequest request = new BlacklistReleaseRequest("메모");
		when(blacklistService.release(any(BlacklistReleaseCommand.class))).thenReturn(detailFixture());

		ResponseEntity<ApiResponse<BlacklistDetailResponse>> response =
				adminBlacklistController.release(adminMember(), 1L, request);

		assertThat(response.getBody().success()).isTrue();
		ArgumentCaptor<BlacklistReleaseCommand> captor = ArgumentCaptor.forClass(BlacklistReleaseCommand.class);
		verify(blacklistService).release(captor.capture());
		assertThat(captor.getValue().releasedBy()).isEqualTo(10L);
	}

	private CustomMember adminMember() {
		return new CustomMember(10L, "ADMIN");
	}

	private CustomMember sellerMember() {
		return new CustomMember(11L, "SELLER");
	}

	private BlacklistSummaryResponse summaryFixture() {
		return new BlacklistSummaryResponse(
				1L,
				"test@test.com",
				"테스트",
				BlacklistStatus.SUSPENDED,
				LocalDateTime.now().minusDays(1),
				LocalDateTime.now().plusDays(6),
				null
		);
	}

	private BlacklistDetailResponse detailFixture() {
		return new BlacklistDetailResponse(
				1L,
				"test@test.com",
				"테스트",
				"01012341234",
				BlacklistStatus.SUSPENDED,
				"사유",
				"메모",
				LocalDateTime.now().minusDays(1),
				LocalDateTime.now().plusDays(6),
				null,
				LocalDateTime.now().minusDays(1),
				LocalDateTime.now()
		);
	}
}
