package com.coc.modi.member.admin.notice.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.common.auth.CustomMember;
import com.coc.modi.member.admin.notice.application.NoticeService;
import com.coc.modi.member.admin.notice.application.dto.NoticeCreateCommand;
import com.coc.modi.member.admin.notice.application.dto.NoticeResponse;
import com.coc.modi.member.admin.notice.application.dto.NoticeUpdateCommand;
import com.coc.modi.member.admin.notice.domain.NoticeStatus;
import com.coc.modi.member.admin.notice.presentation.dto.NoticeCreateRequest;
import com.coc.modi.member.admin.notice.presentation.dto.NoticeUpdateRequest;
import com.coc.modi.member.member.exception.MemberAccessDeniedException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class AdminNoticeControllerTest {

	@Mock
	private NoticeService noticeService;

	@InjectMocks
	private AdminNoticeController adminNoticeController;

	@Test
	void createNotice_requiresAdmin() {
		NoticeCreateRequest request = new NoticeCreateRequest("제목", "내용", null, null, null, null);

		assertThatThrownBy(() -> adminNoticeController.createNotice(sellerMember(), request))
				.isInstanceOf(MemberAccessDeniedException.class);
	}

	@Test
	void createNotice_returnsResponseForAdmin() {
		NoticeCreateRequest request = new NoticeCreateRequest("제목", "내용", true, null, null, null);
		NoticeResponse response = responseFixture(1L);
		when(noticeService.createNotice(any(NoticeCreateCommand.class))).thenReturn(response);

		ResponseEntity<ApiResponse<NoticeResponse>> result =
				adminNoticeController.createNotice(adminMember(), request);

		assertThat(result.getStatusCode().value()).isEqualTo(201);
		assertThat(result.getBody().success()).isTrue();
		assertThat(result.getBody().data().id()).isEqualTo(1L);

		ArgumentCaptor<NoticeCreateCommand> captor = ArgumentCaptor.forClass(NoticeCreateCommand.class);
		verify(noticeService).createNotice(captor.capture());
		assertThat(captor.getValue().createdBy()).isEqualTo(10L);
		assertThat(captor.getValue().pinned()).isTrue();
	}

	@Test
	void updateNotice_requiresAdmin() {
		NoticeUpdateRequest request = new NoticeUpdateRequest("제목", null, null, null, null);

		assertThatThrownBy(() -> adminNoticeController.updateNotice(sellerMember(), 5L, request))
				.isInstanceOf(MemberAccessDeniedException.class);
	}

	@Test
	void updateNotice_callsServiceForAdmin() {
		NoticeUpdateRequest request = new NoticeUpdateRequest("제목", "내용", false, null, null);
		NoticeResponse response = responseFixture(2L);
		when(noticeService.updateNotice(any(NoticeUpdateCommand.class))).thenReturn(response);

		ResponseEntity<ApiResponse<NoticeResponse>> result =
				adminNoticeController.updateNotice(adminMember(), 2L, request);

		assertThat(result.getBody().success()).isTrue();
		assertThat(result.getBody().data().id()).isEqualTo(2L);

		ArgumentCaptor<NoticeUpdateCommand> captor = ArgumentCaptor.forClass(NoticeUpdateCommand.class);
		verify(noticeService).updateNotice(captor.capture());
		assertThat(captor.getValue().noticeId()).isEqualTo(2L);
	}

	@Test
	void deleteNotice_requiresAdmin() {
		assertThatThrownBy(() -> adminNoticeController.deleteNotice(sellerMember(), 3L))
				.isInstanceOf(MemberAccessDeniedException.class);
	}

	@Test
	void deleteNotice_callsServiceForAdmin() {
		ResponseEntity<Void> result = adminNoticeController.deleteNotice(adminMember(), 3L);

		assertThat(result.getStatusCode().value()).isEqualTo(204);
		verify(noticeService).deleteNotice(3L);
	}

	@Test
	void publishNotice_requiresAdmin() {
		assertThatThrownBy(() -> adminNoticeController.publishNotice(sellerMember(), 4L))
				.isInstanceOf(MemberAccessDeniedException.class);
	}

	@Test
	void draftNotice_requiresAdmin() {
		assertThatThrownBy(() -> adminNoticeController.draftNotice(sellerMember(), 4L))
				.isInstanceOf(MemberAccessDeniedException.class);
	}

	@Test
	void publishNotice_callsServiceForAdmin() {
		NoticeResponse response = responseFixture(4L);
		when(noticeService.publishNotice(4L)).thenReturn(response);

		ResponseEntity<ApiResponse<NoticeResponse>> result =
				adminNoticeController.publishNotice(adminMember(), 4L);

		assertThat(result.getBody().success()).isTrue();
		verify(noticeService).publishNotice(4L);
	}

	@Test
	void draftNotice_callsServiceForAdmin() {
		NoticeResponse response = responseFixture(5L);
		when(noticeService.draftNotice(5L)).thenReturn(response);

		ResponseEntity<ApiResponse<NoticeResponse>> result =
				adminNoticeController.draftNotice(adminMember(), 5L);

		assertThat(result.getBody().success()).isTrue();
		verify(noticeService).draftNotice(5L);
	}

	private CustomMember adminMember() {
		return new CustomMember(10L, "ADMIN");
	}

	private CustomMember sellerMember() {
		return new CustomMember(11L, "SELLER");
	}

	private NoticeResponse responseFixture(Long noticeId) {
		return new NoticeResponse(
				noticeId,
				"제목",
				"내용",
				NoticeStatus.DRAFT,
				false,
				0L,
				null,
				null,
				null,
				null
		);
	}
}
