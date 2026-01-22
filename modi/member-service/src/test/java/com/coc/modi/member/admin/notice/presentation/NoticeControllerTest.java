package com.coc.modi.member.admin.notice.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.member.admin.notice.application.NoticeService;
import com.coc.modi.member.admin.notice.application.dto.NoticeResponse;
import com.coc.modi.member.admin.notice.application.dto.NoticeSummaryResponse;
import com.coc.modi.member.admin.notice.domain.NoticeStatus;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class NoticeControllerTest {

	@Mock
	private NoticeService noticeService;

	@InjectMocks
	private NoticeController noticeController;

	@Test
	void getNotices_returnsPage() {
		Pageable pageable = PageRequest.of(0, 10);
		NoticeSummaryResponse summary = new NoticeSummaryResponse(1L, "공지", false, 3L, null);
		Page<NoticeSummaryResponse> page = new PageImpl<>(List.of(summary), pageable, 1);
		when(noticeService.getPublishedNotices("공지", pageable)).thenReturn(page);

		ResponseEntity<ApiResponse<Page<NoticeSummaryResponse>>> result =
				noticeController.getNotices("공지", pageable);

		assertThat(result.getBody().success()).isTrue();
		assertThat(result.getBody().data().getTotalElements()).isEqualTo(1);
		verify(noticeService).getPublishedNotices("공지", pageable);
	}

	@Test
	void getNotice_returnsResponse() {
		NoticeResponse response = new NoticeResponse(
				2L,
				"제목",
				"내용",
				NoticeStatus.PUBLISHED,
				false,
				10L,
				null,
				null,
				null,
				null
		);
		when(noticeService.getPublishedNotice(2L)).thenReturn(response);

		ResponseEntity<ApiResponse<NoticeResponse>> result =
				noticeController.getNotice(2L);

		assertThat(result.getBody().success()).isTrue();
		assertThat(result.getBody().data().id()).isEqualTo(2L);
		verify(noticeService).getPublishedNotice(2L);
	}
}
